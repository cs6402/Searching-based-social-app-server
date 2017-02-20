package com.tdsoft.bro.taguploader.service;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.tdsoft.bro.core.exception.BroException;
import com.tdsoft.bro.core.service.cache.TagCache;
import com.tdsoft.bro.core.service.search.TagSearch;

public class UploadTagTask extends AbstractTask {
	private static final Logger logger = LoggerFactory.getLogger(UploadTagTask.class);
	@Resource(name = "tagCacheImpl")
	private TagCache cache;
	@Resource(name = "tagSearchImpl")
	private TagSearch search;
	@Value("${uploader_tag_max_bytes_length}")
	private long maxBytesLength;
	@Value("${uploader_tag_a_round_time_limit}")
	private long timeLimlt;

	/**
	 * 空白緩衝區長度
	 */
	private long emptyUploadContentLength;
	
	/**
	 * 緩衝區
	 */
	private byte[] accumulatedByteArray = "[".getBytes();
	
	private List<String> allDocumentsInBytes;
	
	@PostConstruct
	public void init() {
		LOG = LoggerFactory.getLogger(UploadTagTask.class);
		try {
			emptyUploadContentLength = "[]".getBytes("UTF-8").length;
			allDocumentsInBytes = new LinkedList<String>();
		} catch (UnsupportedEncodingException e) {
			throw new BroException(e);
		}
	}


	@Override
	public void execute(TaskInfo data) {
		Instant startTime = Instant.now();
		while (true) {
			boolean isTimeUp = upload(startTime, Instant.now().getEpochSecond());
			if (isTimeUp) {
				break;
			} else {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					logger.error("Waiting for looping to upload, but interrupted",e);
				}
			}
		}
		data.setStatus(TaskStatus.DONE);
	}

	/**
	 * @param startTime Task起始時間
	 * @param timeCondition 從cache取得的時間點
	 * @return true 時間到達, false 時間未到達，該timeCondititon已經取不到資料
	 */
	private boolean upload(Instant startTime, long timeCondition) {
		long page = 1;
		while (true) {
			List<SimpleEntry<Double, String>> tags = cache.getTags(timeCondition, page);
			if (tags.isEmpty()) {
				// 無法取到更多的tags時，將緩衝中的tags上傳，並且結束該時間區段的上傳
				try {
					post(timeCondition);
				} catch (Exception e) {
					logger.error("Uploading failed!", e);
				}
				return isTimeUp(startTime);
			}
			for (int i = 0; i < tags.size(); i++) {
				String tag = tags.get(i).getValue();
				Double key = tags.get(i).getKey();
				try {
					if (isTimeUp(startTime)) {
						// 當時間到時，將強制上傳，並且結束該時間區段的上傳
						addToBuffer(tag, key, true);
						return true;
					} else {
						// 將tag寫入緩衝區
						addToBuffer(tag, key, false);
					}
				} catch (Exception e) {
					logger.error("Uploading failed!", e);
				}
			}
			page++;
		}
	}

	/**
	 * 加入tag至緩衝區
	 * @param tag 將tag加入緩衝區
	 * @param score 上傳的最大時間點，此值將刪除cache中的多筆資料
	 * @param forceUpload 是否強制上傳
	 * @throws UnsupportedEncodingException 不會產生，免擔心
	 */
	private void addToBuffer(String tag, double score, boolean forceUpload) throws UnsupportedEncodingException {
		if (accumulatedByteArray.length > maxBytesLength || forceUpload) {
			// 上傳tag，將json陣列尾部符號加上
			byte[] bytes = tag.concat("]").getBytes("UTF-8");
			accumulatedByteArray = concat(accumulatedByteArray, bytes);
			allDocumentsInBytes.add(tag);
			// 判斷有無tag資料，不為空才上傳
			if (accumulatedByteArray.length != emptyUploadContentLength) {
				search.postTags(accumulatedByteArray, allDocumentsInBytes);
				// 清除score前的cache
				cache.removeTags(score);
			}
			// 重置資料，下輪累積資料
			accumulatedByteArray = "[".getBytes();
			allDocumentsInBytes.clear();
		} else {
			byte[] bytes = tag.concat(",").getBytes("UTF-8");
			accumulatedByteArray = concat(accumulatedByteArray, bytes);
			allDocumentsInBytes.add(tag);
		}
	}

	/**
	 * 上傳tag
	 * @param score 上傳的最大時間點，此值將刪除cache中的多筆資料
	 * @throws UnsupportedEncodingException 不會產生，免擔心
	 */
	private void post(double score) throws UnsupportedEncodingException {
		// 有逗號就刪除
		if (accumulatedByteArray[accumulatedByteArray.length - 1] == 44) {
			accumulatedByteArray[accumulatedByteArray.length - 1] = 93;
		} else {
			// 上傳tag，將json陣列尾部符號加上
			byte[] bytes = "]".getBytes("UTF-8");
			accumulatedByteArray = concat(accumulatedByteArray, bytes);	
		}
		
		// 判斷有無tag資料，不為空才上傳
		if (accumulatedByteArray.length != emptyUploadContentLength) {
			search.postTags(accumulatedByteArray, allDocumentsInBytes);
			// 清除score前的cache
			cache.removeTags(score);
		}
		// 重置資料，下輪累積資料
		accumulatedByteArray = "[".getBytes();
		allDocumentsInBytes.clear();
	}
	
	/**
	 * 比對現在時間與參數startTime的差距，判斷是否逾時
	 * @param startTime 起始時間
	 * @return 是否逾時
	 */
	private boolean isTimeUp(Instant startTime) {
		Instant now = Instant.now();
		Duration interval = Duration.between(startTime, now);
		long executingMillis = interval.toMillis();
		if (executingMillis > timeLimlt) {
			return true;
		}
		return false;
	}

	private byte[] concat(byte[] byteArray1, byte[] byteArray2) {
		byte[] result = new byte[byteArray1.length + byteArray2.length];
		System.arraycopy(byteArray1, 0, result, 0, byteArray1.length);
		System.arraycopy(byteArray2, 0, result, byteArray1.length, byteArray2.length);
		return result;
	}
}
