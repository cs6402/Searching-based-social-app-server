package com.tdsoft.bro.dispatcher.service.impl;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.qmsg.UploadTagQMsg;
import com.tdsoft.bro.dispatcher.service.ITagService;

@Service("tagServiceImpl")
public class TagServiceImpl implements ITagService{

	@Resource(name = "amazonSQSClient")
	private AmazonSQS sqsService;
	
	
	@Value("${queue_tag_upload_url}")
	private String tagUploadQueueURL;
	
	@Override
	public String postTags(UploadTagQMsg qmsg) {
		SendMessageRequest smr = new SendMessageRequest();
		smr.setMessageBody(JsonUtils.convertObjectToJson(qmsg));
		smr.setQueueUrl(tagUploadQueueURL);
		SendMessageResult sendMessage = sqsService.sendMessage(smr);
		return sendMessage.getMessageId();
	}

}
