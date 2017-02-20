package com.tdsoft.bro.qhandler.consumer;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:task-config.xml"})
public class QConsumerTaskTest {

	AmazonSQS sqs;

	ReceiveMessageResult rmr;

	@Autowired
	QConsumer qc;
	@Autowired
	QConsumerManager qm;
	@Autowired
	ThreadPoolTaskExecutor executotor;
	Mockery mc;

	@Before
	public void setup() {
		mc = new JUnit4Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};
		sqs = mc.mock(AmazonSQS.class);
		rmr = mc.mock(ReceiveMessageResult.class);
		try {
			qc.setAmazonSQS(sqs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final List<Message> list = new ArrayList<Message>();
		Message m1 = new Message();
		m1.setMessageId("1");
		m1.setBody("m1body");
		list.add(m1);
		
		mc.checking(new Expectations() {
			{
				oneOf(rmr).getMessages();
				will(returnValue(list));

				oneOf(sqs).receiveMessage("abc");
				will(returnValue(rmr));
			}
		});
	}


	@Test
	public void testTaskRun() {
		long sta = System.nanoTime();

		qc.consume();
//		executotor.setWaitForTasksToCompleteOnShutdown(true);
//		executotor.shutdown();
//		qm.remove("sss");
		TaskInfo item = qm.get("task", "1");
		System.out.println((System.nanoTime() - sta) / 1000000);
	}


}
