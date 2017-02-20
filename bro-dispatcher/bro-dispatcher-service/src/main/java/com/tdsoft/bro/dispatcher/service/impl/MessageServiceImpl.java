package com.tdsoft.bro.dispatcher.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.exception.ArgumentInvalidException;
import com.tdsoft.bro.core.manager.MessageManager;
import com.tdsoft.bro.core.qmsg.BroadcastQMsg;
import com.tdsoft.bro.core.qmsg.ChatQMsg;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.DeviceMessageCache;
import com.tdsoft.bro.dispatcher.service.IMessageService;

@Service("messageServiceImpl")
public class MessageServiceImpl implements IMessageService {
	@Resource(name = "amazonSQSClient")
	private AmazonSQS sqsService;

	@Value("${queue_message_broadcast_url}")
	private String broadcastQueueURL;

	@Value("${queue_message_chat_url}")
	private String chatQueueURL;

	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache cacheService;

	@Resource(name = "deviceReceivedMessageCacheImpl")
	private DeviceMessageCache deviceReceivedMessageCache;
	
	@Resource(name = "messageManager")
	private MessageManager messageManager;

	@Override
	public String broadcast(BroadcastQMsg qmsg) {
		// 以下註解邏輯為將經緯度轉為點，根據半徑取得半徑內所有的點，目前將點的邏輯重構於DeviceLocationCache
		// double[] point = LocationUtils.convertLatLonToMercator(qmsg.getLatitude(),
		// qmsg.getLongtitude());
		// long pointx = LocationUtils.convertMecratorToPoint(point[0], point[1]);
		// int radius = qmsg.getRadius();
		// 計算範圍內的點
		// Set<Long> range = LocationUtils.getRange(pointx, radius);
		// Long[] points = qmsg.getPoints();
		// for (Long p : points) {
		// if (!range.contains(p)) {
		// // 拋錯
		// throw new ArgumentInvalidException("Points doesn't match with redius and point");
		// }
		// }

		SendMessageRequest smr = new SendMessageRequest();
		smr.setMessageBody(JsonUtils.convertObjectToJson(qmsg));
		smr.setQueueUrl(broadcastQueueURL);
		SendMessageResult sendMessage = sqsService.sendMessage(smr);
		return sendMessage.getMessageId();
	}

	@Override
	public String chat(ChatQMsg qmsg) {
		DeviceInfoCacheBean deviceInfo = ContextUtils.getDeviceInfo();
		String senderId = deviceInfo.getDeviceId();
		// validate sender
		if (StringUtils.equals(senderId, qmsg.getSenderDeviceId())) {
			// validate receiver
			Long expireTime = cacheService.getExireTime(qmsg.getReceiverDeviceId());
			if (expireTime > 1000 || expireTime == 0) {
				// less than a day = 24 hours 目前暫時不檢查
				// if (expireTime < 24) {
				// cacheService.putDeviceInfo(qmsg.getReceiverDeviceId(), null);
				// }
				SendMessageRequest smr = new SendMessageRequest();
				String body = JsonUtils.convertObjectToJson(qmsg);
				smr.setMessageBody(body);
				smr.setQueueUrl(chatQueueURL);
				// put directly
				SendMessageResult sendMessage = sqsService.sendMessage(smr);
				DeviceMessageCacheBean bean = new DeviceMessageCacheBean();
				bean.setContent(body);
				bean.setTimestamp(qmsg.getTimestamp());
				deviceReceivedMessageCache.putDeviceMessage(qmsg.getReceiverDeviceId(), bean);
				Map<String, String> notifyInfo = messageManager.getNotifyInfo(qmsg.getReceiverDeviceId());
				if (!notifyInfo.isEmpty()) {
					notifyInfo.forEach((type, serviceKey) -> {
						// 目前直接採用裝置，未來可能增加web，並傳入當參數
						messageManager.notify(serviceKey, qmsg.getReceiverDeviceId());
					});
				}
				return sendMessage.getMessageId();
			} else {
				throw new ArgumentInvalidException("receiver doesn't exist!");
			}
		} else {
			throw new ArgumentInvalidException("sender doesn't exist!");
		}
	}
}
