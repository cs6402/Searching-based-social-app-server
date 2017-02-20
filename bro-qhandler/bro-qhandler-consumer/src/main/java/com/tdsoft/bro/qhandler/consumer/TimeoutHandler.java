package com.tdsoft.bro.qhandler.consumer;

import javax.annotation.PostConstruct;
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
	@Resource(name = "qConsumerManager")
	private QConsumerManager qConsumerManager;
	
	private boolean isTraceEnabled;
	/**
	 * 初始化TimeoutHandler
	 */
	@PostConstruct
	public void init() {
		isTraceEnabled = LOG.isTraceEnabled();
	}
	
	/**
	 * 清除
	 * 時間增加000變成毫秒計算
	 */
	@Scheduled(fixedRateString = "${check_task_if_timeout_interval}000")
	public void handle() {
		if (isTraceEnabled) {
			LOG.trace("before clean up, cahce size: {}", qConsumerManager.getAllSize());
			qConsumerManager.cleanUp();
			LOG.trace("clean up finished, cahce size: {}", qConsumerManager.getAllSize());
		} else {
			qConsumerManager.cleanUp();
		}
	}
}
