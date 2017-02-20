package com.tdsoft.bro.taguploader.service;

import java.io.Serializable;

public class TaskInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private Thread executingThread;
	private TaskStatus status = TaskStatus.READY;
	private String taskId;

	public Thread getExecutingThread() {
		return executingThread;
	}

	public void setExecutingThread(Thread executingThread) {
		this.executingThread = executingThread;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

}
