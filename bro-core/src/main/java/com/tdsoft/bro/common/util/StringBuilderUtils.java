package com.tdsoft.bro.common.util;

/**
 * 僅用於單一Service與簡短字串連接用(不使用於log輸出)，須注意巢狀呼叫時會清掉內容
 * @author Daniel
 *
 */
public class StringBuilderUtils {
	
	private static final ThreadLocal<StringBuilder> threadLocalBuilder = new ThreadLocal<StringBuilder>() {
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}

		public StringBuilder get() {
			StringBuilder b = super.get();
			b.setLength(0); // clear/reset the buffer
			return b;
		}

	};

	/**
	 * 取得該Thread共用StringBuilder
	 * @return
	 */
	public static StringBuilder getStringBuilder() {
		return threadLocalBuilder.get();
	}
}
