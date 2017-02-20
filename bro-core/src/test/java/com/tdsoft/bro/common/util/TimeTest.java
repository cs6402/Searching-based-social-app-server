package com.tdsoft.bro.common.util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TimeTest {
	@Test
	public void test() throws IOException, InterruptedException {
		System.out.println(Instant.now().toEpochMilli());
		LocalDateTime now = LocalDateTime.now();
		System.out.println(System.currentTimeMillis());
//		System.out.println(now.getEpochSecond());
		System.out.println(now.get(ChronoField.MILLI_OF_DAY));
		Thread.sleep(200);
		now = LocalDateTime.now();
		System.out.println(System.nanoTime());
		System.out.println(now.get(ChronoField.MILLI_OF_DAY));
		Thread.sleep(1000);
		Thread.sleep(1);
		now = LocalDateTime.now();
		System.out.println(System.nanoTime());
//		System.out.println(now.getEpochSecond());
		System.out.println(now.get(ChronoField.MILLI_OF_DAY));

//		while (true) {
//			extracted("GMT-1:00");
//			extracted("GMT+8:00");
//			Thread.sleep(1000);
//			System.out.println("============");
//		}
	}

	private void extracted(String id) {
		TimeZone tz = TimeZone.getTimeZone(id);
		Calendar calendar = Calendar.getInstance(tz);
		System.out.println(calendar.getTimeInMillis());
	
		Date date = new Date(calendar.getTimeInMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
		formatter.setTimeZone(tz);
		String dateFormatted = formatter.format(date);
		System.out.println(dateFormatted);
	}
}
