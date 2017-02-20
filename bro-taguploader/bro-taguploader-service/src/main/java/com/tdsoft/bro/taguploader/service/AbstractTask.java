package com.tdsoft.bro.taguploader.service;

import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Runnable {
	protected static Logger LOG;

	private BlockingQueue<TaskInfo> messagePendingQueue;
	private TaskInfo taskInfo;
	@Resource(name = "taskManager")
	private TaskManager taskManager;
	private String taskName;

	public void run() {
		try {
			while (true) {
				taskInfo = messagePendingQueue.take();
				taskInfo.setExecutingThread(Thread.currentThread());
				taskInfo.setStatus(TaskStatus.PROCESSING);
				LOG.info("Executing task for uploading! Thread: {}", taskInfo.getExecutingThread());
				taskManager.put(taskInfo.getTaskId(), taskInfo);
				execute(taskInfo);
				notifyTaskFinished();
			}
		} catch (InterruptedException e) {
			LOG.error(this.getClass().getSimpleName() + " Task interrupted failed and exited:", e);
		} catch (Exception e) {
			LOG.error(this.getClass().getSimpleName() + " Task operation failed and exited:", e);
		} finally {
			doWorkBeforeShutdown();
		}
	}

	@PostConstruct
	protected void init() {
		LOG = LoggerFactory.getLogger(getTaskName());
	}

	protected void doWorkBeforeShutdown() {
		// TODO maybe send email
		LOG.info("Hello doWorkBeforeShutdown");
	}

	protected void notifyTaskFinished() {
		taskManager.remove(taskInfo);
	}

	abstract public void execute(TaskInfo data);

	public BlockingQueue<TaskInfo> getMessagePendingQueue() {
		return messagePendingQueue;
	}

	public void setMessagePendingQueue(BlockingQueue<TaskInfo> messagePendingQueue) {
		this.messagePendingQueue = messagePendingQueue;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setTaskManager(TaskManager qConsumerManager) {
		this.taskManager = qConsumerManager;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
