package com.tdsoft.bro.tagcenter.service.impl;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagSearchResultBean;
import com.tdsoft.bro.tagcenter.service.ITagService;

/**
 * TODO 暫時不用之後改為uploader使用
 * @author dliu
 *
 */
@Deprecated
public class TagServiceImpl implements ITagService{
	@Resource(name = "sqsClient")
	AmazonSQS sqs;
	@Value("${core_search_queue_url}")
	private String queueURL;
	
	@Override
	public void postTags(TagSearchResultBean bean) {
		String json = JsonUtils.convertObjectToJson(bean);
		SendMessageRequest req = new SendMessageRequest();
		req.setMessageBody(json);
		req.setQueueUrl(queueURL);
		sqs.sendMessage(req);
	}

}
