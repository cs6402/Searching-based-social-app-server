package com.tdsoft.bro.qhandler.consumer.task;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.qmsg.ChatQMsg;

//@RunWith(JUnit4.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:task-config.xml"})
public class ChatMessageTaskTest {
	@Test
	public void t() {
		while(true) {}
	}
//	@Test
	public void test() {
		Monitor m = MonitorFactory.start("");

		String body = "";
		ChatQMsg qmsg = JsonUtils.convertJsonToObject(body, ChatQMsg.class);

		
		m.stop();
		System.out.println(m.getActive());
	}
}
