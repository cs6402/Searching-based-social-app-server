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

import com.tdsoft.bro.core.qmsg.ChatQMsg;
import com.tdsoft.bro.dispatcher.dto.ChatMessageDTO;
import com.tdsoft.bro.dispatcher.dto.MessageSuccessDTO;
import com.tdsoft.bro.dispatcher.service.IMessageService;

@Controller
public class ChatCtrl {
	@Resource(name = "messageServiceImpl")
	IMessageService msgService;

	@RequestMapping(value = "${ctrl_chat}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> chat(@Valid @RequestBody ChatMessageDTO dto) {
		ChatQMsg qmsg = new ChatQMsg();
		qmsg.setContent(dto.getContent());
		qmsg.setSenderDeviceId(dto.getSenderDeviceId());
		qmsg.setReceiverDeviceId(dto.getReceiverDeviceId());
		qmsg.setClientMessageId(dto.getClientMessageId());
		qmsg.setTimestamp(Instant.now().getEpochSecond());
		String messageId = msgService.chat(qmsg);
		MessageSuccessDTO resp = new MessageSuccessDTO();
		resp.setMessageId(messageId);
		resp.setTimestamp(qmsg.getTimestamp());
		return new ResponseEntity<>(resp, HttpStatus.CREATED);
	}
}
