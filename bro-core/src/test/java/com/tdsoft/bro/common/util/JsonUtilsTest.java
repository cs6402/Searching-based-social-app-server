package com.tdsoft.bro.common.util;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.core.bean.TagCSCacheBean;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.bean.TagESSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;

@RunWith(JUnit4.class)
public class JsonUtilsTest {
	@Test
	public void t() {
		TagCacheBean b = new TagCacheBean();
		b.setField(null);
		b.setId("ad");
		System.out.println(JsonUtils.convertObjectToJson(b));
	}
	
	public void test() throws IOException {
		TagCacheBean tb = new TagCacheBean();
		TagSearchBean d = new TagESSearchBean();
		tb.setId("03");
		d.setDeviceId("abc");
		d.setDeviceType(DeviceType.APNS);
		d.setPoint(1111l);
		d.setSnsToken("bcd");
		d.setTags(new String[]{"hi", "mm"});
		long al = 123l;
		long bl = 321l;
//		d.setLocation(String.format("%d,%d", al,bl));
		tb.setField(d);
		String convertObjectToJson = JsonUtils.convertObjectToJson(tb);
		System.out.println(convertObjectToJson);
		TagCacheBean convertJsonToObject = JsonUtils.convertJsonToObject(convertObjectToJson, TagCacheBean.class);
		System.out.println(convertJsonToObject);
		String a = "1,d0247fcc-f1ac-31b8-9fa1-e5eeb4e3e01e";
		String t = "{\"S\":\"d0247fcc-f1ac-31b8-9fa1-e5eeb4e3e01e\",\"T\":\"GCM\"}";
		long na = System.nanoTime();
		for (int i  = 0 ; i<10000000; i++) {
			char charAt = a.charAt(0);
			String substring = a.substring(2);
//			Location l = JsonUtils.convertJsonToObject(t, Location.class);
//			l.getSnstoken();l.getType();
		}
		long s = System.nanoTime() - na;
		System.out.println(s/1000000);
	}
	
	public static class Location implements Serializable{
		@JsonProperty("S")
		private String snstoken;
		@JsonProperty("T")
		private String type;
		
		public String getSnstoken() {
			return snstoken;
		}
		public void setSnstoken(String snstoken) {
			this.snstoken = snstoken;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}
