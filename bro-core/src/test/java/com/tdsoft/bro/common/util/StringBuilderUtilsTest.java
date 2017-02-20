package com.tdsoft.bro.common.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StringBuilderUtilsTest {
	@Test
	public void testThreadlocal() throws InterruptedException, ExecutionException {
		StringBuilder stringBuilder = StringBuilderUtils.getStringBuilder();
		Assert.assertNotNull(stringBuilder);
		StringBuilder theSameStringBuilder = StringBuilderUtils.getStringBuilder();
		Assert.assertEquals(stringBuilder, theSameStringBuilder);
		FutureTask<StringBuilder> secTask = new FutureTask<StringBuilder>(new Callable<StringBuilder>() {

			public StringBuilder call() throws Exception {
				return StringBuilderUtils.getStringBuilder();
			}
		});
		Thread secThread = new Thread(secTask);
		secThread.start();
		StringBuilder diffStringBuilder = secTask.get();
		Assert.assertNotEquals(stringBuilder, diffStringBuilder);
	}
	
}
