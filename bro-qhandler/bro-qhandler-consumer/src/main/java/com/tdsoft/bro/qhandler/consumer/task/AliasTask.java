package com.tdsoft.bro.qhandler.consumer.task;

import org.slf4j.LoggerFactory;

import com.amazonaws.services.sqs.model.Message;
import com.tdsoft.bro.common.CodeConstant;
import com.tdsoft.bro.core.exception.JsonFormatException;
import com.tdsoft.bro.qhandler.consumer.AbstractConsumerTask;
import com.tdsoft.bro.qhandler.consumer.TaskInfo;
import com.tdsoft.bro.qhandler.consumer.TaskStatus;

public class AliasTask extends AbstractConsumerTask {
	private boolean isDebugEnabled;

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
//			String body = message.getBody();
//			BroadcastQMsg qmsg = JsonUtils.convertJsonToObject(body, BroadcastQMsg.class);
//			broadcastService.broadcast(message.getMessageId(), qmsg, body);
			data.setStatus(TaskStatus.DONE);
			if (isDebugEnabled)
				LOG.debug(sbLog.toString());
		} catch (JsonFormatException e) {
			sbLog.insert(0, "Convert json to object failed!");
			LOG.error(sbLog.toString(), e);
			data.setStatus(TaskStatus.FAILED);
		} catch (Exception e) {
			sbLog.insert(0, "Broadcast failed!");
			LOG.error(sbLog.toString(), e);
			data.setStatus(TaskStatus.FAILED);
		}
	}

}
