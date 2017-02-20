package com.tdsoft.bro.taguploader.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定時任務-清除cache
 * @author Daniel
 *
 */
@Service("timeoutMonitor")
public class TimeoutHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TimeoutHandler.class);
	/**
	 * Cache管理類
	 */
	@Resource(name = "taskManager")
	private TaskManager taskManager;

	/**
	 * 清除
	 * 時間增加000變成毫秒計算
	 */
	@Scheduled(fixedRateString = "${uploader_monitor_rate}000")
	public void handle() {
		LOG.trace("before clean up, cahce size: {}", taskManager.size());
		taskManager.cleanUp();
		LOG.trace("clean up finished, cahce size: {}", taskManager.size());
	}
}
