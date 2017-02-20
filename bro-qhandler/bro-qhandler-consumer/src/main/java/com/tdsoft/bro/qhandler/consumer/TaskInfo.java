package com.tdsoft.bro.qhandler.consumer;

import java.io.Serializable;

import com.amazonaws.services.sqs.model.Message;

public class TaskInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int timeoutCount = 0;
	private Thread executingThread;
	private Message message;
	private TaskStatus status = TaskStatus.READY;
	private String queueUrl;
	
	public void addTimeoutCount() {
		timeoutCount++;
	}
	
	public int getTimeoutCount() {
		return timeoutCount;
	}

	public void setTimeoutCount(int timeoutCount) {
		this.timeoutCount = timeoutCount;
	}

	public Thread getExecutingThread() {
		return executingThread;
	}

	public void setExecutingThread(Thread executingThread) {
		this.executingThread = executingThread;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getQueueUrl() {
		return queueUrl;
	}

	public void setQueueUrl(String queueUrl) {
		this.queueUrl = queueUrl;
	}
}
