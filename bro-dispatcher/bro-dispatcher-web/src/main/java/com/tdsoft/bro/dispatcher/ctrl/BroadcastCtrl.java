package com.tdsoft.bro.dispatcher.ctrl;

import java.time.Instant;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.qmsg.BroadcastQMsg;
import com.tdsoft.bro.dispatcher.dto.BroadcastMessageDTO;
import com.tdsoft.bro.dispatcher.dto.MessageSuccessDTO;
import com.tdsoft.bro.dispatcher.service.IMessageService;

@Controller
public class BroadcastCtrl {

	@Resource(name = "messageServiceImpl")
	IMessageService msgService;

	/**
	 * 發送廣播訊息
	 * 
	 * @param dto
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "${ctrl_broadcast}", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> broadcast(@Valid @RequestBody BroadcastMessageDTO dto) {
		DeviceInfoCacheBean die = ContextUtils.getDeviceInfo();
		BroadcastQMsg qmsg = new BroadcastQMsg();
		qmsg.setContent(dto.getContent());
		qmsg.setCreatetime(Instant.now().getEpochSecond());
		qmsg.setDeviceId(die.getDeviceId());
		qmsg.setDeviceNo(die.getDeviceNo());
		qmsg.setLatitude(dto.getLatitude());
		qmsg.setLongtitude(dto.getLongtitude());
		qmsg.setMessageType(dto.getMessageType());
		qmsg.setRadius(dto.getRadius());
		qmsg.setTitle(dto.getTitle());

		String messageId = msgService.broadcast(qmsg);
		MessageSuccessDTO resp = new MessageSuccessDTO();
		resp.setMessageId(messageId);
		resp.setTimestamp(qmsg.getCreatetime());
		return new ResponseEntity<>(resp, HttpStatus.CREATED);
	}
}
