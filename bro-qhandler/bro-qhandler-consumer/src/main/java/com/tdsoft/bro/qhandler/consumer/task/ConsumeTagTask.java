package com.tdsoft.bro.qhandler.consumer.task;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.Message;
import com.tdsoft.bro.common.CodeConstant;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.exception.JsonFormatException;
import com.tdsoft.bro.core.qmsg.UploadTagQMsg;
import com.tdsoft.bro.qhandler.consumer.AbstractConsumerTask;
import com.tdsoft.bro.qhandler.consumer.TaskInfo;
import com.tdsoft.bro.qhandler.consumer.TaskStatus;
import com.tdsoft.bro.qhandler.service.ITagService;

public class ConsumeTagTask extends AbstractConsumerTask {
	private boolean isDebugEnabled;
	@Resource(name = "tagServiceImpl")
	private ITagService tagService;

	@Override
	protected void init() {
		LOG = LoggerFactory.getLogger(getTaskName());
		isDebugEnabled = LOG.isDebugEnabled();		
	}

	@Override
	public void execute(TaskInfo data) {
		StringBuilder sbLog = new StringBuilder();
		sbLog.append("Task:").append(this.getClass().getName()).append(CodeConstant.LINE_SEPARATOR);
		try {
			Message message = data.getMessage();
			sbLog.append("Message:").append(message.toString()).append(CodeConstant.LINE_SEPARATOR);
			String body = message.getBody();
			UploadTagQMsg qmsg = JsonUtils.convertJsonToObject(body, UploadTagQMsg.class);
			tagService.consumeTag(qmsg, body);
			data.setStatus(TaskStatus.DONE);
			if (isDebugEnabled)
				LOG.debug(sbLog.toString());
		} catch (JsonFormatException e) {
			sbLog.insert(0, "Convert json to object failed!");
			LOG.error(sbLog.toString(), e);
			data.setStatus(TaskStatus.FAILED);
		} catch (Exception e) {
			sbLog.insert(0, "Consuming Tag failed!");
			LOG.error(sbLog.toString(), e);
			data.setStatus(TaskStatus.FAILED);
		}
	}

}
