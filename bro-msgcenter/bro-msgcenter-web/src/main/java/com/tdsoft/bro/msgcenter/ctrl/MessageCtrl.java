package com.tdsoft.bro.msgcenter.ctrl;


import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.PlusDeferredResult;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.common.util.LocationUtils;
import com.tdsoft.bro.core.bean.BroadcastMessageCacheBean;
import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.manager.MessageManager;
import com.tdsoft.bro.core.service.cache.BroMessageCache;
import com.tdsoft.bro.core.service.cache.DeviceMessageCache;
import com.tdsoft.bro.msgcenter.dto.BroadcastMessageDTO;
import com.tdsoft.bro.msgcenter.dto.ChatMessageDTO;
import com.tdsoft.bro.msgcenter.dto.MessageStatusDTO;

@Controller
@RequestMapping(method = RequestMethod.GET)
public class MessageCtrl {

	@Resource(name = "deviceReceivedMessageCacheImpl")
	DeviceMessageCache msgReceivedCache;

	@Resource(name = "deviceSentMessageCacheImpl")
	DeviceMessageCache msgSentCache;

	@Resource(name = "broMessageCacheImpl")
	BroMessageCache broCache;
	
	@Resource(name = "messageManager")
	private MessageManager messageManager;

	@RequestMapping(value = "${ctrl_device_received_messages}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public DeferredResult<List<ChatMessageDTO>> retrieveMessages(HttpServletRequest request, @PathVariable("deviceId") String deviceId,
			@PathVariable("lastUpdateTime") Long lastUpdateTime, @Valid @DecimalMin("1") @PathVariable("page") Long page) {
		PlusDeferredResult<List<ChatMessageDTO>> deferredResponse = new PlusDeferredResult<List<ChatMessageDTO>>();
		List<DeviceMessageCacheBean> deviceMessage = msgReceivedCache.getDeviceMessage(deviceId, lastUpdateTime, page);
		if (deviceMessage.isEmpty() && page == 1) {
			deferredResponse.setRetrieveMessageCallback(() -> {
				if (messageManager.releaseNotify(deviceId)) {
					List<DeviceMessageCacheBean> deviceMessage2 = msgReceivedCache.getDeviceMessage(deviceId, lastUpdateTime, page);
					List<ChatMessageDTO> result = new ArrayList<ChatMessageDTO>(deviceMessage2.size());
					deviceMessage2.stream().forEach(e -> {
						String content = e.getContent();
						result.add(JsonUtils.convertJsonToObject(content, ChatMessageDTO.class));
					});
					deferredResponse.setResult(result);
				}
			});
			
			deferredResponse.onTimeout(deferredResponse.getRetrieveMessageCallback());
			messageManager.needToNotify(deviceId, deferredResponse);
			return deferredResponse;
		} else {
			List<ChatMessageDTO> result = new ArrayList<ChatMessageDTO>(deviceMessage.size());
			deviceMessage.stream().forEach(e -> {
				String content = e.getContent();
				result.add(JsonUtils.convertJsonToObject(content, ChatMessageDTO.class));
			});
			deferredResponse.setResult(result);
			return deferredResponse;
		}
	}

	@RequestMapping(value = "${ctrl_device_sent_messages}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MessageStatusDTO> checkMessageSuccessful(@PathVariable("deviceId") String deviceId,
			@PathVariable("clientMessageId") Long clientMessgeId) {
		List<DeviceMessageCacheBean> deviceMessage = msgSentCache.getDeviceMessage(deviceId, clientMessgeId, 0l);
		MessageStatusDTO dto = new MessageStatusDTO();
		dto.setSuccessful(!deviceMessage.isEmpty());
		return new ResponseEntity<MessageStatusDTO>(dto, HttpStatus.OK);
	}

	@RequestMapping(value = "${ctrl_broadcast_messages}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<BroadcastMessageDTO>> getAllBroadcastMessages(@PathVariable("latitude") Long latitude,
			@PathVariable("longtitude") Long longtitude, @PathVariable("lastUpdateTime") Long lastUpdateTime,
			@PathVariable("page") Long page) {
		double[] pointXY = LocationUtils.convertLatLonToMercator(latitude, longtitude);
		long point = LocationUtils.convertMecratorToPoint(pointXY[0], pointXY[1]);
		List<BroadcastMessageCacheBean> broadcastMessaage = broCache.getBroadcastMessaages(String.valueOf(point), lastUpdateTime, page);
		List<BroadcastMessageDTO> result = new ArrayList<BroadcastMessageDTO>(broadcastMessaage.size());
		broadcastMessaage.stream().forEach(e -> {
			String content = e.getContent();
			result.add(JsonUtils.convertJsonToObject(content, BroadcastMessageDTO.class));
		});
		return new ResponseEntity<List<BroadcastMessageDTO>>(result, HttpStatus.OK);
	}
}
