package com.tdsoft.bro.qhandler.consumer;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.tdsoft.bro.common.util.SpringContextUtil;

/**
 * 從AmazonSQS取回訊息並放置隊列
 * @author Daniel
 *
 */
public class QConsumer {
	/**
	 * Amazon SQS client
	 */
	@Resource(name = "amazonSQSClient")
	private AmazonSQS amazonSQS;
	/**
	 * 存放取回訊息隊列
	 */
	private BlockingQueue<TaskInfo> messagePendingQueue;
	/**
	 * 執行序池
	 */
	@Resource(name = "taskExecutor")
	private ThreadPoolTaskExecutor executotor;
	/**
	 * 負責處理此隊列之任務名稱
	 */
	private String taskName;
	/**
	 * 逾時時間設定
	 */
	private int timeout;
	/**
	 * 日誌工具
	 */
	private Logger LOG;

	private ReceiveMessageRequest receiveMessageRequest;
	// TODO 邏輯：收到訊息直接持久化？
	// 每處理完一筆直接發送訊息
	// 刪除訊息功能持久化？
	// 增加中控
	// 採用全持久化與flag判斷
	
	/**
	 * 從AmazonSQS取回多筆訊息並放置隊列
	 */
	public void consume() {
		LOG.debug("start to consume SQS queue");
		try {
			ReceiveMessageResult receiveMessage = amazonSQS.receiveMessage(receiveMessageRequest);
			List<Message> messages = receiveMessage.getMessages();
			LOG.debug("Recevice result message counts: {} ", messages.size());
			Iterator<Message> iterator = messages.iterator();
			while (iterator.hasNext()) {
				Message message = iterator.next();
				TaskInfo taskInfo = new TaskInfo();
				taskInfo.setMessage(message);
				taskInfo.setQueueUrl(receiveMessageRequest.getQueueUrl());
				getMessagePendingQueue().offer(taskInfo);
			}
		} catch (Exception e) {
			LOG.error("Exception raised while QConsumer invoke consume, Task: " + taskName, e);
		}
	}
	
	@Resource(name = "qConsumerManager")
	private QConsumerManager manager;
	
	
	/**
	 * 初始化
	 * 1.指定日誌工具的名稱
	 * 2.產生任務Cache
	 * 3.產生任務執行序
	 */
	@PostConstruct
	public void initialize(){
		LOG = LoggerFactory.getLogger(taskName);
		
		manager.createTaskCache(taskName, timeout);
		
		String threadNamePrefix = executotor.getThreadNamePrefix();
		executotor.setThreadNamePrefix(taskName);
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		for (int i = 0; i < availableProcessors * 10; i++) {
			executotor.setBeanName(taskName + i);
			executotor.execute((Runnable) SpringContextUtil.getBean(taskName));
		}
		executotor.setThreadNamePrefix(threadNamePrefix);
		LOG.debug("Created {} work thread to execute {}", availableProcessors, taskName);
		
	}

	public AmazonSQS getAmazonSQS() {
		return amazonSQS;
	}

	public void setAmazonSQS(AmazonSQS amazonSQS) {
		this.amazonSQS = amazonSQS;
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

	public ReceiveMessageRequest getReceiveMessageRequest() {
		return receiveMessageRequest;
	}

	public void setReceiveMessageRequest(ReceiveMessageRequest receiveMessageRequest) {
		this.receiveMessageRequest = receiveMessageRequest;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

}
