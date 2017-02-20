package com.tdsoft.bro.taguploader.service;

import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.tdsoft.bro.common.util.SpringContextUtil;
import com.tdsoft.bro.core.service.cache.TagCache;

/**
 * 
 * @author Daniel
 *
 */
public class TokenReceiver {
	
	@Resource(name = "tagCacheImpl")
	private TagCache cache;
	/**
	 * 存放取回Token隊列
	 */
	private BlockingQueue<TaskInfo> messagePendingQueue;
	/**
	 * 執行序池
	 */
	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executotor;
	/**
	 * 任務名稱
	 */
	private String taskName;
	/**
	 * 日誌工具
	 */
	private Logger LOG;

	public void execute() {
		LOG.debug("start to get Task token");
		try {
			while (true) {
				String taskId = cache.getTagToken();
				TaskInfo taskInfo = new TaskInfo();
				taskInfo.setTaskId(taskId);
				getMessagePendingQueue().offer(taskInfo);
			}
		} catch (Exception e) {
			LOG.error("Exception raised while TokenReceiver invoke execute, Task: " + taskName, e);
		}
	}

	/**
	 * 初始化 1.指定日誌工具的名稱 2.產生任務執行序
	 */
	@PostConstruct
	public void initialize() {
		LOG = LoggerFactory.getLogger(taskName);
		String threadNamePrefix = executotor.getThreadNamePrefix();
		executotor.setThreadNamePrefix(taskName);
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < 2; i++) {
			executotor.setBeanName(taskName + i);
			executotor.execute((Runnable) SpringContextUtil.getBean(taskName));
		}
		executotor.setThreadNamePrefix(threadNamePrefix);
		LOG.debug("Created {} work thread to execute {}", availableProcessors, taskName);
	}

	public BlockingQueue<TaskInfo> getMessagePendingQueue() {
		return messagePendingQueue;
	}

	public void setMessagePendingQueue(BlockingQueue<TaskInfo> messagePendingQueue) {
		this.messagePendingQueue = messagePendingQueue;
	}

	public ThreadPoolTaskExecutor getExecutotor() {
		return executotor;
	}

	public void setExecutotor(ThreadPoolTaskExecutor executotor) {
		this.executotor = executotor;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
