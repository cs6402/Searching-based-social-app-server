package com.tdsoft.bro.qhandler.consumer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.tdsoft.bro.common.CodeConstant;

/**
 * QConsume中控管理 控制所有逾時任務
 * 
 * @author Daniel
 * 
 */
@Service("qConsumerManager")
public class QConsumerManager {
	private static final Logger LOG = LoggerFactory.getLogger(QConsumerManager.class);
	/**
	 * 逾時任務最高次數
	 */
	@Value("${task_execution_timeout_maxxium_count}")
	private int timeoutMaxCount = 3;

	/**
	 * 放置所有任務訊息的cache
	 */
	private Map<String, Cache<String, TaskInfo>> cacheMap;
	
	/**
	 * Amazon SQS client
	 */
	@Resource(name = "amazonSQSClient")
	private AmazonSQS amazonSQS;

	/**
	 * LOG debug level設定
	 */
	private boolean isDebugEnabled = LOG.isDebugEnabled();
	private boolean isTraceEnabled = LOG.isTraceEnabled();
	@PostConstruct
	public void init() {
		cacheMap = new ConcurrentHashMap<String, Cache<String, TaskInfo>>();
	}

	public void createTaskCache(final String taskName, int timeout) {
		/**
		 * 訊息遮蔽時間設定
		 */
		final int visibilityTimeout = timeout * 2;
		cacheMap.put(
				taskName,
				CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS)
						.removalListener(new RemovalListener<String, TaskInfo>() {

							public void onRemoval(RemovalNotification<String, TaskInfo> nof) {
								handleRemovedOrExpired(taskName, nof.getKey(), nof.getValue(), visibilityTimeout);
							}
						}).build());
	}
	
	/**
	 * 處理逾時與刪除事件
	 * 
	 * @param messageId 訊息代號MessageId
	 * @param taskInfo 任務訊息
	 */
	private void handleRemovedOrExpired(String taskName, String messageId, TaskInfo taskInfo, int visibilityTimeout) {
		// 任務執行中，延長時間或中斷任務
		if (taskInfo.getStatus() == TaskStatus.PROCESSING) {
			if (taskInfo.getTimeoutCount() >= timeoutMaxCount) {
				// remove and interrupt
				try {
					taskInfo.getExecutingThread().interrupt();
				} catch (Exception e) {
					LOG.error("Stopping task failed!", e);
				}

				StringBuilder sb = new StringBuilder();
				sb.append("Consuming task exceeded timeout! Details: ");
				sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
				sb.append(" Message:").append(taskInfo.getMessage().toString());
				LOG.error(sb.toString());

			} else {
				ChangeMessageVisibilityRequest changeMessageVisibilityRequest =
						new ChangeMessageVisibilityRequest().withQueueUrl(taskInfo.getQueueUrl())
								.withReceiptHandle(taskInfo.getMessage().getReceiptHandle()).withVisibilityTimeout(visibilityTimeout);
				StringBuilder sb = new StringBuilder();
				sb.append("Consuming task exceeded timeout! Counts:").append(taskInfo.getTimeoutCount()).append(" Details: ");
				sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
				sb.append(" Message:").append(taskInfo.getMessage().toString());
				LOG.warn(sb.toString());
				taskInfo.addTimeoutCount();
				// change unfinish message visiablity
				amazonSQS.changeMessageVisibility(changeMessageVisibilityRequest);
				put(taskName, messageId, taskInfo);
				sb.setLength(0);
				sb.append("Sent ChangeMessageVisibilityRequest finished! Content: ").append(
						changeMessageVisibilityRequest.toString());
				LOG.warn(sb.toString());
			}
		}  
	}

	public void put(String taskName, String messageId, TaskInfo taskInfo) {
		cacheMap.get(taskName).put(messageId, taskInfo);
	}

	public void remove(String taskName, TaskInfo taskInfo) {
		if (taskInfo.getStatus() == TaskStatus.DONE) {
			DeleteMessageRequest deleteMessageRequest =
					new DeleteMessageRequest().withQueueUrl(taskInfo.getQueueUrl()).withReceiptHandle(
							taskInfo.getMessage().getReceiptHandle());
			amazonSQS.deleteMessage(deleteMessageRequest);
			if (isTraceEnabled) {
				LOG.trace("Sent DeleteMessageRequest finished! Content: {}", deleteMessageRequest.toString());
			}
			if (isDebugEnabled) {
				StringBuilder sb = new StringBuilder();
				sb.append("Remove item after done ");
				sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
				sb.append(" Message:").append(taskInfo.getMessage().toString());
				LOG.debug(sb.toString());
			}
		} else if (taskInfo.getStatus() == TaskStatus.FAILED) {
			if (isDebugEnabled) {
				StringBuilder sb = new StringBuilder();
				sb.append("Remove item after failed ");
				sb.append(" Thread id:").append(taskInfo.getExecutingThread().getName());
				sb.append(" Message:").append(taskInfo.getMessage().toString());
				LOG.warn(sb.toString());
			}
		}
		cacheMap.get(taskName).invalidate(taskInfo.getMessage().getMessageId());
	}

	public int getTimeoutMaxCount() {
		return timeoutMaxCount;
	}

	public void setTimeoutMaxCount(int timeoutMaxCount) {
		this.timeoutMaxCount = timeoutMaxCount;
	}

	public void sendDeleteMessage() {

	}

	public TaskInfo get(String taskName, String messageId) {
		return cacheMap.get(taskName).getIfPresent(messageId);
	}

	public void cleanUp() {
		for (Cache<String, TaskInfo> cache : cacheMap.values()) {
			cache.cleanUp();
		}
	}

	public String getAllSize() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String,Cache<String,TaskInfo>> e : cacheMap.entrySet()) {
			sb.append(CodeConstant.LINE_SEPARATOR).append("\t").append(e.getKey()).append(": ").append(e.getValue().size());
		}
		return sb.toString();
	}
}
