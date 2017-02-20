package com.tdsoft.bro.core.service.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.service.cache.DeviceMessageCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class DeviceMessageCacheTest {
	
	@Resource(name = "deviceReceivedMessageCacheImpl")
	private DeviceMessageCache cache;
	
//	@Test
	public void addDatas() {
		DeviceMessageCacheBean bean = new DeviceMessageCacheBean();
		bean.setContent(UUID.randomUUID().toString());
		bean.setTimestamp(System.nanoTime());
		Monitor monitor = MonitorFactory.getMonitor("Result", "put").start();
		
		for (long i = 0 ; i < 4294967295l; i++) {
			cache.putDeviceMessage("test", bean);
			bean.setContent(UUID.randomUUID().toString());
			bean.setTimestamp(System.nanoTime());
		}
		monitor.stop();
		System.out.println(monitor.toString());
	}
	@Test
	public void getData() {
//		Monitor monitor = MonitorFactory.start();
		long score = 7544860243998l; 
		long total = 0;
		while (true) {
			Monitor monitora = MonitorFactory.start("a");
			List<DeviceMessageCacheBean> deviceMessage = cache.getDeviceMessage("test", score, 1l);
			DeviceMessageCacheBean deviceMessageCacheBean = deviceMessage.get(deviceMessage.size() - 1);
			score = deviceMessageCacheBean.getTimestamp();
			total+=deviceMessage.size();
			if (deviceMessage.isEmpty() || total == 100) {
				monitora.stop();
				System.out.println(monitora.getAvg());
				break;
			}

		}
//		monitor.stop();
//		System.out.println(total);
//		System.out.println(monitor.getAvg());
	}

}
