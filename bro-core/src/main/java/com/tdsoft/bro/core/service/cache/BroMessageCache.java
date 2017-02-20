package com.tdsoft.bro.core.service.cache;

import java.util.List;

import com.tdsoft.bro.core.bean.BroadcastMessageCacheBean;

public interface BroMessageCache extends CacheService {
	/**
	 * 廣播訊息存入
	 * 
	 * @param point 點
	 * @param message 廣播訊息內容
	 */
	void putBroadcastMessage(String point, BroadcastMessageCacheBean message);

	/**
	 * 清除過期訊息
	 * 
	 * @param point 點
	 * @param now 現在時間
	 */
	void cleanBroadcastMessage(String point, Long timestamp);

	/**
	 * 根據點取得全部廣播訊息代號
	 * 
	 * @param point 點
	 * @param previouslyGetTimestamp 上次訊息標籤
	 * @return 該點全部廣播訊息代號
	 */
	List<BroadcastMessageCacheBean> getBroadcastMessaages(String point, Long previouslyGetTimestamp, Long page);
	
}
