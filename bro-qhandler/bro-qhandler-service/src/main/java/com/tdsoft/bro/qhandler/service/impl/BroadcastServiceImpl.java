package com.tdsoft.bro.qhandler.service.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.tdsoft.bro.common.CodeConstant;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.common.util.LocationUtils;
import com.tdsoft.bro.common.util.StringBuilderUtils;
import com.tdsoft.bro.core.bean.BroadcastMessageCacheBean;
import com.tdsoft.bro.core.bean.DeviceLocationCacheBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.dao.MessageDao;
import com.tdsoft.bro.core.entity.BroMessageEntity;
import com.tdsoft.bro.core.qmsg.BroadcastQMsg;
import com.tdsoft.bro.core.service.cache.BroMessageCache;
import com.tdsoft.bro.core.service.cache.DeviceLocationCache;
import com.tdsoft.bro.core.service.search.TagSearch;
import com.tdsoft.bro.qhandler.service.IBroadcastService;
import com.tdsoft.bro.qhandler.service.bean.BroadcastPushContent;

@Service("broadcastServiceImpl")
public class BroadcastServiceImpl implements IBroadcastService {
	private static final Logger LOG = LoggerFactory.getLogger(BroadcastServiceImpl.class);

	@Resource(name = "messageDao")
	private MessageDao messageDao;

	@Resource(name = "broMessageCacheImpl")
	BroMessageCache broMessageCache;
	
	@Resource(name = "deviceLocationCacheImpl")
	DeviceLocationCache deviceLocationCache;

	@Resource(name = "amazonSNSClient")
	private AmazonSNS snsService;

	@Resource(name = "amazonSQSClient")
	private AmazonSQS sqsService;

	@Value("${service_device_android_target_arn_prefix}")
	private String andoridTargetArnPrefix;
	@Value("${service_device_baidu_target_arn_prefix}")
	private String baiduTargetArnPrefix;
	@Value("${service_device_ios_target_arn_prefix}")
	private String iosTargetArnPrefix;
	@Value("${service_message_maximum_size}")
	private int messageMaximumSize;
	
	@Value("${service_device_SNS_unknown_queue}")
	private String deviceSNSUnknownQueueURL;
	
	@Resource(name = "tagSearchImpl")
	private TagSearch searchService;
	
	private boolean isDebugEnabled = LOG.isDebugEnabled();

	/**
	 * 根據Cache處理廣播訊息
	 * ※目前因為範圍取點算法有誤，故從搜索服務為基礎進行處理廣播訊息
	 * @param messageId 訊息代號
	 * @param qmsg 廣播訊息物件
	 * @param body 廣播訊息JSON格式字串
	 */
	@SuppressWarnings("unused")
	private void handleBroadcastRequestByCache(String messageId, BroadcastQMsg qmsg, String body) {
		// 計算範圍內的點
		double[] pointXY = LocationUtils.convertLatLonToMercator(qmsg.getLatitude(), qmsg.getLongtitude());
		long pointX = LocationUtils.convertMecratorToPoint(pointXY[0], pointXY[1]);
		int radius = qmsg.getRadius();
		Set<Long> pointsInRange = LocationUtils.getRange(pointX, radius);
		
		// 1. insert message to DB
		// 2. prepare push content
		// 3. get device from cache
		// 4. put message to cache
		// 5. broadcast

		BroMessageEntity me = new BroMessageEntity();
		me.setContent(qmsg.getContent());
		Long createtime = qmsg.getCreatetime();
		Date createDate = new Date(createtime);
		me.setCreatetime(createDate);
		me.setDeviceNo(qmsg.getDeviceNo());
		me.setLatitude(qmsg.getLatitude());
		me.setLongtitude(qmsg.getLongtitude());
		me.setMessageType(qmsg.getMessageType());
		me.setRadius(qmsg.getRadius());
		me.setTitle(qmsg.getTitle());
		me.setClientMessageId(messageId);
		messageDao.save(me);
		
		StringBuilder sbLog = new StringBuilder();
		
		String contentFit = truncateWhenUTF8(qmsg.getContent(), messageMaximumSize);
		BroadcastPushContent broadcastPushContent = new BroadcastPushContent();
		broadcastPushContent.setContent(contentFit);
		broadcastPushContent.setLatitude(qmsg.getLatitude());
		broadcastPushContent.setLongtitude(qmsg.getLongtitude());
		broadcastPushContent.setMessageType(qmsg.getMessageType());
		broadcastPushContent.setTitle(qmsg.getTitle());
		broadcastPushContent.setClientMessageId(messageId);
		String pushContent = JsonUtils.convertObjectToJson(broadcastPushContent);
		List<DeviceLocationCacheBean> unknownSNSDevices = new LinkedList<>();

		for (Long point : pointsInRange) {
			String sPoint = String.valueOf(point);
			List<DeviceLocationCacheBean> devices = deviceLocationCache.getDeviceLocationsByPoint(sPoint);
			
			if (devices.isEmpty()) {
				continue;
			}
			// 存入該位置點的訊息Cache
			BroadcastMessageCacheBean bmcb = new BroadcastMessageCacheBean();
			bmcb.setContent(body);
			bmcb.setTimestamp(createtime);
			broMessageCache.putBroadcastMessage(sPoint, bmcb);
			
			StringBuilder sbArn = StringBuilderUtils.getStringBuilder();
			for (DeviceLocationCacheBean de : devices) {
				// 未避開發送者，算了
				switch (de.getDeviceType()) {
					case APNS:
						sbArn.append(iosTargetArnPrefix);
						break;
					case BAIDU:
						sbArn.append(baiduTargetArnPrefix);
						break;
					case GCM:
						sbArn.append(andoridTargetArnPrefix);
						break;
					default:
						LOG.warn("Error DeviceType. Device: {}", de.toString());
						continue;
				}
				PublishRequest e = new PublishRequest();
				e.setTargetArn(sbArn.append(de.getSnsToken()).toString());
				e.setMessage(pushContent);
				try {
					snsService.publish(e);
					sbLog.append("TargetArn:").append(e.getTargetArn()).append(CodeConstant.LINE_SEPARATOR);
				} catch(Exception ex) {
					unknownSNSDevices.add(de);
					sbLog.append("Failed to Pushing to TargetArn:").append(e.getTargetArn()).append(CodeConstant.LINE_SEPARATOR);
				}
			}
		}
		// 無法找到的SNS end point的裝置資訊，代表沒清乾淨
		if (!unknownSNSDevices.isEmpty()) {
			try {
				SendMessageBatchRequest batch = new SendMessageBatchRequest(deviceSNSUnknownQueueURL);
				List<SendMessageBatchRequestEntry> entries = batch.getEntries();
				for (DeviceLocationCacheBean bean : unknownSNSDevices) {
					String json = JsonUtils.convertObjectToJson(bean);
					SendMessageBatchRequestEntry entity = new SendMessageBatchRequestEntry();
					entity.setMessageBody(json);
					entries.add(entity);
				}
				sqsService.sendMessageBatch(batch);
			} catch (Exception ex) {
				LOG.error("Failed to send batch messages to SQS while unknown SNS device exist.", ex);
			}
		}
		sbLog.append("Message:").append(qmsg.toString());
		if (isDebugEnabled)
			LOG.debug(sbLog.toString());

	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public void broadcast(String messageId, BroadcastQMsg msg, String body) {
		handleBroadcastRequestBySearch(messageId, msg, body);
	}

	/**
	 * 根據搜索服務處理廣播訊息
	 * @param messageId 訊息代號
	 * @param qmsg 廣播訊息物件
	 * @param body 廣播訊息JSON格式字串
	 */
	private void handleBroadcastRequestBySearch(String messageId, BroadcastQMsg qmsg, String body) {
		
		// 1. insert message to DB
		// 2. prepare push content
		// 3. get device from cache
		// 4. put message to cache
		// 5. broadcast

		BroMessageEntity me = new BroMessageEntity();
		me.setContent(qmsg.getContent());
		Long createtime = qmsg.getCreatetime();
		Date createDate = new Date(createtime);
		me.setCreatetime(createDate);
		me.setDeviceNo(qmsg.getDeviceNo());
		me.setLatitude(qmsg.getLatitude());
		me.setLongtitude(qmsg.getLongtitude());
		me.setMessageType(qmsg.getMessageType());
		me.setRadius(qmsg.getRadius());
		me.setTitle(qmsg.getTitle());
		me.setClientMessageId(messageId);
		messageDao.save(me);
		
		StringBuilder sbLog = new StringBuilder();
		
		String contentFit = truncateWhenUTF8(qmsg.getContent(), messageMaximumSize);
		BroadcastPushContent broadcastPushContent = new BroadcastPushContent();
		broadcastPushContent.setContent(contentFit);
		broadcastPushContent.setLatitude(qmsg.getLatitude());
		broadcastPushContent.setLongtitude(qmsg.getLongtitude());
		broadcastPushContent.setMessageType(qmsg.getMessageType());
		broadcastPushContent.setTitle(qmsg.getTitle());
		broadcastPushContent.setClientMessageId(messageId);
		String pushContent = JsonUtils.convertObjectToJson(broadcastPushContent);
		List<TagSearchBean> unknownSNSDevices = new LinkedList<>();
		long page = 1;
		Set<Long> hasBeenSentPoints = new HashSet<Long>();
		while (true) {
			List<TagSearchBean> retrieveNearDeviceInfo = searchService.retrieveNearDeviceInfosByTagSearch(qmsg.getLatitude(), qmsg.getLongtitude(), page);
			
			if (retrieveNearDeviceInfo.isEmpty()) {
				break;
			}

			StringBuilder sbArn = StringBuilderUtils.getStringBuilder();
			for (TagSearchBean deviceInfoFromTagSearch : retrieveNearDeviceInfo) {
				long point = deviceInfoFromTagSearch.getPoint();
				
				if (!hasBeenSentPoints.contains(point)) {
					// 存入該位置點的訊息Cache
					BroadcastMessageCacheBean bmcb = new BroadcastMessageCacheBean();
					bmcb.setContent(body);
					bmcb.setTimestamp(createtime);
					broMessageCache.putBroadcastMessage(String.valueOf(point), bmcb);
					hasBeenSentPoints.add(point);
				}
				if (deviceInfoFromTagSearch.getDeviceId().equals(qmsg.getDeviceId())) {
					// 避開發送者
					continue;
				}
				switch (deviceInfoFromTagSearch.getDeviceType()) {
					case APNS:
						sbArn.append(iosTargetArnPrefix);
						break;
					case BAIDU:
						sbArn.append(baiduTargetArnPrefix);
						break;
					case GCM:
						sbArn.append(andoridTargetArnPrefix);
						break;
					default:
						LOG.warn("Error DeviceType. Device: {}", deviceInfoFromTagSearch.toString());
						continue;
				}
				PublishRequest e = new PublishRequest();
				e.setTargetArn(sbArn.append(deviceInfoFromTagSearch.getSnsToken()).toString());
				e.setMessage(pushContent);
				try {
					snsService.publish(e);
					sbLog.append("TargetArn:").append(e.getTargetArn()).append(CodeConstant.LINE_SEPARATOR);
				} catch(Exception ex) {
					unknownSNSDevices.add(deviceInfoFromTagSearch);
					sbLog.append("Failed to Pushing to TargetArn:").append(e.getTargetArn()).append(CodeConstant.LINE_SEPARATOR);
				}
			}
			page++;
		}
		// 無法找到的SNS end point的裝置資訊，代表沒清乾淨
		if (!unknownSNSDevices.isEmpty()) {
			try {
				SendMessageBatchRequest batch = new SendMessageBatchRequest(deviceSNSUnknownQueueURL);
				List<SendMessageBatchRequestEntry> entries = batch.getEntries();
				for (TagSearchBean bean : unknownSNSDevices) {
					String json = JsonUtils.convertObjectToJson(bean);
					SendMessageBatchRequestEntry entity = new SendMessageBatchRequestEntry();
					entity.setMessageBody(json);
					entries.add(entity);
				}
				sqsService.sendMessageBatch(batch);
			} catch (Exception ex) {
				LOG.error("Failed to send batch messages to SQS while unknown SNS device exist.", ex);
			}
		}
		sbLog.append("Message:").append(qmsg.toString());
		if (isDebugEnabled)
			LOG.debug(sbLog.toString());

	}
	
	private static String truncateWhenUTF8(String s, int maxBytes) {
	    int b = 0;
	    for (int i = 0; i < s.length(); i++) {
	        char c = s.charAt(i);

	        int skip = 0;
	        int more;
	        if (c <= 0x007f) {
	            more = 1;
	        }
	        else if (c <= 0x07FF) {
	            more = 2;
	        } else if (c <= 0xd7ff) {
	            more = 3;
	        } else if (c <= 0xDFFF) {
	            more = 4;
	            skip = 1;
	        } else {
	            more = 3;
	        }

	        if (b + more > maxBytes) {
	            return s.substring(0, i);
	        }
	        b += more;
	        i += skip;
	    }
	    return s;
	}

}
