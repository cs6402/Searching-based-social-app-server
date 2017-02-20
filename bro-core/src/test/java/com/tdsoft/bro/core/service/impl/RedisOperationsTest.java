package com.tdsoft.bro.core.service.impl;


import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.impl.DeviceReceivedMessageCacheImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class RedisOperationsTest {


	@Before
	public void setup() {}

	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache impl;

	@Resource(name = "deviceReceivedMessageCacheImpl")
	private DeviceReceivedMessageCacheImpl d;
	@Test
	public void testCache() {
		Monitor m = MonitorFactory.start("");
		for (int i =0;i<500;i++) {
			
//			DeviceInfoCacheBean deviceInfoForPush = impl.getDeviceInfoForPush("1138819804752832943@B");
//			Assert.assertNotNull(deviceInfoForPush);
			DeviceMessageCacheBean dm = new DeviceMessageCacheBean();
			dm.setContent("HIddddddddddddddddddd");
			dm.setTimestamp(System.nanoTime());
			d.putDeviceMessage("1138819804752832943@B", dm);
		}
		m.stop();
		System.out.println(m.getLastValue());
	}
}
