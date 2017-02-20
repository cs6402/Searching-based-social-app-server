package com.tdsoft.bro.qhandler.consumer;

import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.slf4j.Logger;

public abstract class AbstractConsumerTask implements Runnable {
	protected Logger LOG;


	private BlockingQueue<TaskInfo> messagePendingQueue;
	private TaskInfo taskInfo;
	@Resource(name = "qConsumerManager")
	private QConsumerManager qConsumerManager;
	private String taskName;

	public void run() {
		try {
			while (true) {
				taskInfo = messagePendingQueue.take();
				taskInfo.setExecutingThread(Thread.currentThread());
				taskInfo.setStatus(TaskStatus.PROCESSING);
				qConsumerManager.put(taskName, taskInfo.getMessage().getMessageId(), taskInfo);
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
	
	abstract protected void init();

	protected void doWorkBeforeShutdown() {
		// TODO maybe send email
		LOG.info("Hello doWorkBeforeShutdown");
	}

	protected void notifyTaskFinished() {
		qConsumerManager.remove(taskName, taskInfo);
	}

	abstract public void execute(TaskInfo data);

	public BlockingQueue<TaskInfo> getMessagePendingQueue() {
		return messagePendingQueue;
	}

	public void setMessagePendingQueue(BlockingQueue<TaskInfo> messagePendingQueue) {
		this.messagePendingQueue = messagePendingQueue;
	}

	public QConsumerManager getQConsumerManager() {
		return qConsumerManager;
	}

	public void setQConsumerManager(QConsumerManager qConsumerManager) {
		this.qConsumerManager = qConsumerManager;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
