package com.tdsoft.bro.qhandler.service.impl;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.tdsoft.bro.common.CodeConstant;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.dao.DeviceInfoDao;
import com.tdsoft.bro.core.qmsg.ChatQMsg;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.DeviceMessageCache;
import com.tdsoft.bro.qhandler.service.IChatService;

@Service("chatServiceImpl")
public class ChatServiceImpl implements IChatService {
	private static final Logger LOG = LoggerFactory.getLogger(ChatServiceImpl.class);

	@Resource(name = "amazonSNSClient")
	private AmazonSNS snsService;

	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache deviceInfoCache;

	@Resource(name = "deviceReceivedMessageCacheImpl")
	private DeviceMessageCache deviceReceivedMessageCache;

	@Resource(name = "deviceSentMessageCacheImpl")
	private DeviceMessageCache deviceSentMessageCache;

	@Value("${service_device_android_target_arn_prefix}")
	private String andoridTargetArnPrefix;
	@Value("${service_device_baidu_target_arn_prefix}")
	private String baiduTargetArnPrefix;
	@Value("${service_device_ios_target_arn_prefix}")
	private String iosTargetArnPrefix;

	@Value("${service_message_maximum_size}")
	private int messageMaximumSize;

	@Resource(name = "deviceInfoDao")
	private DeviceInfoDao deviceInfoDao;

	@Value("${service_message_clean_size}")
	private int pageSize;

	private boolean isDebugEnabled = LOG.isDebugEnabled();

	@Override
	public void chat(String messageId, ChatQMsg qmsg, String body) {
		StringBuilder sbLog = new StringBuilder();
		String receiverDeviceId = qmsg.getReceiverDeviceId();
		String senderDeviceId = qmsg.getSenderDeviceId();
		long clientMessageId = Long.parseLong(qmsg.getClientMessageId());
		DeviceInfoCacheBean receiverDeviceInfo = deviceInfoCache.getDeviceInfoForPush(receiverDeviceId);
		String snsToken = receiverDeviceInfo.getSnsToken();
		DeviceType type = receiverDeviceInfo.getType();
		StringBuilder sbArn = new StringBuilder();
		switch (type) {
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
				LOG.warn("Error DeviceType. Device: {}", type.toString());
				return;
		}
		// �蝵格������敹怠��
		DeviceMessageCacheBean bean = new DeviceMessageCacheBean();
		bean.setContent(body);
		bean.setTimestamp(qmsg.getTimestamp());
		// ��ds�摮
		// deviceReceivedMessageCache.putDeviceMessage(receiverDeviceId, bean);
		// �蝵桃��神�閮敹怠��imestamp��雿輻��隞����(鋆蔭蝡舐����)
		bean.setTimestamp(clientMessageId);
		deviceSentMessageCache.putDeviceMessage(senderDeviceId, bean);
		// ���
		try {
			PublishRequest publishReq = new PublishRequest();
			publishReq.setTargetArn(sbArn.append(snsToken).toString());
			String contentFit = truncateWhenUTF8(qmsg.getContent(), messageMaximumSize);
			qmsg.setContent(contentFit);
			String content = JsonUtils.convertObjectToJson(qmsg);
			publishReq.setMessage(content);
			snsService.publish(publishReq);
			sbLog.append("TargetArn:").append(publishReq.getTargetArn()).append(CodeConstant.LINE_SEPARATOR).append("Message:")
					.append(content);
			if (isDebugEnabled)
				LOG.debug(sbLog.toString());
		} catch (Exception e) {
			// ����仃������蝡航�蔭�銵閰�
			LOG.info("Pushing message failed while chating", e);
		}
	}

	private static String truncateWhenUTF8(String s, int maxBytes) {
		int b = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			int skip = 0;
			int more;
			if (c <= 0x007f) {
				more = 1;
			} else if (c <= 0x07FF) {
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

	@Override
	public void clean(Long timestamp) {
		LOG.info("Start to clean message, timestamp: {}", timestamp);
		PageRequest firstPage = new PageRequest(0, pageSize);
		internallyClean(timestamp, firstPage);
	}

	private void internallyClean(Long timestamp, Pageable pageable) {
		Page<String> page = deviceInfoDao.findAllPaged(pageable);
		page.getContent().forEach(e -> {
			try {
				deviceReceivedMessageCache.delDeviceMessage(e, timestamp);
				deviceSentMessageCache.delDeviceMessage(e, timestamp);
				LOG.info("Cleaned message, deviceId:{} , timestamp:{}", e, timestamp);
			} catch (Exception ex) {
				LOG.error("Failed to clean message, deviceId:{} , timestamp:{}, error:{}", e, timestamp, ex.getMessage());
			}
		});
		if (page.hasNext()) {
			internallyClean(timestamp, page.nextPageable());
		}
	}
}
