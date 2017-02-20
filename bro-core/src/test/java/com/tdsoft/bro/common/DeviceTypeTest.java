package com.tdsoft.bro.common;

import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeviceTypeTest {
	@Test
	public void testConvert() {
		long n = System.currentTimeMillis() / 1000L;
//		Instant plus = Instant.now().plus(7 * 24, ChronoUnit.HOURS);
		Instant plus = Instant.now();
		System.out.println(plus.toEpochMilli());
		String[] arr = {"G", "A", "B"};
		for (int i = 0; i < 100000000; i++) {
			int j = (int) (Math.random() * 3);
			DeviceType.fromString(arr[j]);
		}
	}

	@Test
	public void testConvertByString() {
		String[] arr = {"GCM", "APNS", "BAIDU"};
		for (int i = 0; i < 100000000; i++) {
			int j = (int) (Math.random() * 3);
			DeviceType.valueOf(arr[j]);
		}
	}
}
