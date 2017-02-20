package com.tdsoft.bro.taguploader.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.tdsoft.bro.core.service.cache.TagCache;

/**
 * QConsume中控管理 控制所有逾時任務
 * 
 * @author Daniel
 * 
 */
@Service("taskManager")
public class TaskManager {
	private static final Logger LOG = LoggerFactory.getLogger(TaskManager.class);

	@Resource(name = "tagCacheImpl")
	private TagCache tagCache;

	/**
	 * 放置所有任務訊息的cache
	 */
	private Cache<String, TaskInfo> cache;
	/**
	 * 逾時時間設定
	 */
	@Value("${uploader_task_timeout}")
	private int timeout;

	/**
	 * LOG debug level設定
	 */
	private boolean isDebugEnabled = LOG.isDebugEnabled();

	/**
	 * 初始化cache
	 */
	@PostConstruct
	public void initialize() {
		cache =
				CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS)
						.removalListener(new RemovalListener<String, TaskInfo>() {

							public void onRemoval(RemovalNotification<String, TaskInfo> nof) {
								handleRemovedOrExpired(nof.getKey(), nof.getValue());
							}
						}).build();
	}

	/**
	 * 處理事件狀況
	 * 
	 * @param messageId 訊息代號MessageId
	 * @param taskInfo 任務訊息
	 */
	private void handleRemovedOrExpired(String taskId, TaskInfo taskInfo) {
		switch (taskInfo.getStatus()) {
			case DONE:
				if (isDebugEnabled) {
					StringBuilder sb = new StringBuilder();
					sb.append("Remove item after done ");
					sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
					sb.append(" Message:").append(taskInfo.toString());
					LOG.debug(sb.toString());
				}
				break;
			case FAILED:
				if (isDebugEnabled) {
					StringBuilder sb = new StringBuilder();
					sb.append("Remove item after failed ");
					sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
					sb.append(" Message:").append(taskInfo.toString());
					LOG.warn(sb.toString());
				}
				break;
			case PROCESSING:
				// 任務執行中，中斷任務
				try {
					taskInfo.getExecutingThread().interrupt();
				} catch (Exception e) {
					LOG.error("Stopping task failed!", e);
				}
				StringBuilder sb = new StringBuilder();
				sb.append("Task exceeded timeout! Details: ");
				sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
				sb.append(" Task Id:").append(taskInfo.getTaskId());
				LOG.error(sb.toString());
				break;
			case READY:
				break;
			default:
				break;

		}
		tagCache.releaseTagToken();
	}

	public void put(String taskId, TaskInfo taskInfo) {
		cache.put(taskId, taskInfo);
	}

	public void remove(TaskInfo taskInfo) {
		cache.invalidate(taskInfo.getTaskId());
	}

	public TaskInfo get(String messageId) {
		return cache.getIfPresent(messageId);
	}

	public void cleanUp() {
		cache.cleanUp();
	}

	public long size() {
		return cache.size();
	}
}
