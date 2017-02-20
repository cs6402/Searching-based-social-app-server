package com.tdsoft.bro.qhandler.consumer;


import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AddPermissionRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequest;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchRequestEntry;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityBatchResult;
import com.amazonaws.services.sqs.model.ChangeMessageVisibilityRequest;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ListDeadLetterSourceQueuesRequest;
import com.amazonaws.services.sqs.model.ListDeadLetterSourceQueuesResult;
import com.amazonaws.services.sqs.model.ListQueuesRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.RemovePermissionRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import com.google.common.hash.Hashing;
import static com.google.common.base.Preconditions.checkArgument;

public class MockSQS implements AmazonSQS {
	private final Map<String, Queue<MessageInfo>> queues = new HashMap<String, Queue<MessageInfo>>();
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(10);
	private int timeout = 35 * 60;
	private final Map<String, ScheduledMessage> receivedMessages = new HashMap<String, ScheduledMessage>();


	public SendMessageResult sendMessage(final SendMessageRequest request) throws AmazonServiceException, AmazonClientException {
		final Queue<MessageInfo> queue = getOrCreateQueue(request.getQueueUrl());
		final MessageInfo info = new MessageInfo();
		info.body = checkNotNull(request.getMessageBody());
		info.id = UUID.randomUUID().toString();

		if (request.getDelaySeconds() == null) {
			queue.add(info);
		} else {
			Runnable task = new Runnable() {
				public void run() {
					queue.add(info);
				}
			};
			executor.schedule(task, request.getDelaySeconds(), TimeUnit.SECONDS);
		}

		return new SendMessageResult().withMessageId(info.id).withMD5OfMessageBody(info.hash());
	}



	public ReceiveMessageResult receiveMessage(ReceiveMessageRequest request) throws AmazonServiceException, AmazonClientException {
		final Queue<MessageInfo> queue = getOrCreateQueue(request.getQueueUrl());
		List<Message> messages = new ArrayList<Message>();

		Integer max = request.getMaxNumberOfMessages();
		if (max == null) {
			max = 0;
		}
		checkArgument(max <= 10 && max > 0);

		Integer visibilityTimeout = request.getVisibilityTimeout();
		if (visibilityTimeout == null) {
			visibilityTimeout = timeout;
		}

		for (int i = 0; i < max; ++i) {
			final MessageInfo info = queue.poll();

			if (info != null) {
				final String receiptHandle = UUID.randomUUID().toString();

				Message message = new Message();
				message.setBody(info.body);
				message.setMessageId(info.id);
				message.setMD5OfBody(info.hash());
				message.setReceiptHandle(receiptHandle);
				messages.add(message);

				Runnable command = new Runnable() {
					public void run() {
						queue.add(info);
						receivedMessages.remove(receiptHandle);
					}
				};

				ScheduledMessage scheduled = new ScheduledMessage();
				scheduled.future = executor.schedule(command, visibilityTimeout, TimeUnit.SECONDS);
				scheduled.runnable = command;
				receivedMessages.put(message.getReceiptHandle(), scheduled);
			}
		}

		return new ReceiveMessageResult().withMessages(messages);
	}

	public void deleteMessage(DeleteMessageRequest request) throws AmazonServiceException, AmazonClientException {
		ScheduledMessage scheduled = receivedMessages.remove(request.getReceiptHandle());
		if (scheduled == null) {
			throw new RuntimeException("message does not exist");
		}

		scheduled.future.cancel(true);
	}

	public void changeMessageVisibility(ChangeMessageVisibilityRequest request) throws AmazonServiceException, AmazonClientException {
		ScheduledMessage scheduled = receivedMessages.get(request.getReceiptHandle());
		if (scheduled == null) {
			throw new RuntimeException("message does not exist");
		}

		scheduled.future.cancel(true);
		scheduled.future = executor.schedule(scheduled.runnable, checkNotNull(request.getVisibilityTimeout()).longValue(), TimeUnit.SECONDS);
	}

	public void shutdown() {
		executor.shutdown();
		receivedMessages.clear();
		queues.clear();
	}

	private static class MessageInfo {
		String body;
		String id;

		String hash() {
			return Hashing.md5().hashString(body, Charset.defaultCharset()).toString();
		}
	}

	private static class ScheduledMessage {
		ScheduledFuture future;
		Runnable runnable;
	}

	private Queue<MessageInfo> getOrCreateQueue(String url) {
		Queue<MessageInfo> queue = queues.get(checkNotNull(url));

		if (queue == null) {
			synchronized (queues) {
				queue = queues.get(checkNotNull(url));

				if (queue == null) {
					queue = new ArrayDeque<MessageInfo>();
					queues.put(url, queue);
				}
			}
		}

		return queue;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setEndpoint(String endpoint) throws IllegalArgumentException {


	}

	public void setRegion(Region region) throws IllegalArgumentException {


	}

	public void setQueueAttributes(SetQueueAttributesRequest setQueueAttributesRequest) throws AmazonServiceException,
			AmazonClientException {


	}

	public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(
			ChangeMessageVisibilityBatchRequest changeMessageVisibilityBatchRequest) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public GetQueueUrlResult getQueueUrl(GetQueueUrlRequest getQueueUrlRequest) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void removePermission(RemovePermissionRequest removePermissionRequest) throws AmazonServiceException, AmazonClientException {


	}

	public GetQueueAttributesResult getQueueAttributes(GetQueueAttributesRequest getQueueAttributesRequest) throws AmazonServiceException,
			AmazonClientException {

		return null;
	}

	public SendMessageBatchResult sendMessageBatch(SendMessageBatchRequest sendMessageBatchRequest) throws AmazonServiceException,
			AmazonClientException {

		return null;
	}

	public ListDeadLetterSourceQueuesResult listDeadLetterSourceQueues(ListDeadLetterSourceQueuesRequest listDeadLetterSourceQueuesRequest)
			throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void deleteQueue(DeleteQueueRequest deleteQueueRequest) throws AmazonServiceException, AmazonClientException {


	}

	public ListQueuesResult listQueues(ListQueuesRequest listQueuesRequest) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public DeleteMessageBatchResult deleteMessageBatch(DeleteMessageBatchRequest deleteMessageBatchRequest) throws AmazonServiceException,
			AmazonClientException {

		return null;
	}

	public CreateQueueResult createQueue(CreateQueueRequest createQueueRequest) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void addPermission(AddPermissionRequest addPermissionRequest) throws AmazonServiceException, AmazonClientException {


	}

	public ListQueuesResult listQueues() throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void setQueueAttributes(String queueUrl, Map<String, String> attributes) throws AmazonServiceException, AmazonClientException {


	}

	public ChangeMessageVisibilityBatchResult changeMessageVisibilityBatch(String queueUrl,
			List<ChangeMessageVisibilityBatchRequestEntry> entries) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void changeMessageVisibility(String queueUrl, String receiptHandle, Integer visibilityTimeout) throws AmazonServiceException,
			AmazonClientException {


	}

	public GetQueueUrlResult getQueueUrl(String queueName) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void removePermission(String queueUrl, String label) throws AmazonServiceException, AmazonClientException {


	}

	public GetQueueAttributesResult getQueueAttributes(String queueUrl, List<String> attributeNames) throws AmazonServiceException,
			AmazonClientException {

		return null;
	}

	public SendMessageBatchResult sendMessageBatch(String queueUrl, List<SendMessageBatchRequestEntry> entries)
			throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void deleteQueue(String queueUrl) throws AmazonServiceException, AmazonClientException {


	}

	public SendMessageResult sendMessage(String queueUrl, String messageBody) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public ReceiveMessageResult receiveMessage(String queueUrl) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public ListQueuesResult listQueues(String queueNamePrefix) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public DeleteMessageBatchResult deleteMessageBatch(String queueUrl, List<DeleteMessageBatchRequestEntry> entries)
			throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public CreateQueueResult createQueue(String queueName) throws AmazonServiceException, AmazonClientException {

		return null;
	}

	public void addPermission(String queueUrl, String label, List<String> aWSAccountIds, List<String> actions)
			throws AmazonServiceException, AmazonClientException {


	}

	public void deleteMessage(String queueUrl, String receiptHandle) throws AmazonServiceException, AmazonClientException {


	}

	public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest request) {

		return null;
	}

}
