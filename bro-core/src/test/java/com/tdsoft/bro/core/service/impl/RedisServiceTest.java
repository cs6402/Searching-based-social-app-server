package com.tdsoft.bro.core.service.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.common.DeviceInfoCacheKeyType;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.entity.BroMessageEntity;
import com.tdsoft.bro.core.exception.JsonFormatException;
import com.tdsoft.bro.core.service.cache.BroMessageCache;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.DeviceLocationCache;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class RedisServiceTest {
	BroMessageCache broMessageCache;
	DeviceInfoCache deviceInfoCache;
	DeviceLocationCache deviceLocationCache;


	@Before
	public void setup() {}

	@Resource(name = "broadcastMessageRedisDeviceClient")
	private RedisTemplate<String, String> deviceLocationRedisDeviceClient;

	@Test
	public void testZscan() {
		RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
		long nanoTime = System.nanoTime();
		byte[] point = "pointx".getBytes();
		Set<byte[]> zRange = c.zRange(point, 0, -1);
		// Cursor<Tuple> zScan = c.zScan(point, ScanOptions.NONE);
		// long count = 0;
		// while (zScan.hasNext()) {
		// Tuple next = zScan.next();
		// String s = new String(next.getValue());
		// count ++;
		// }
		System.out.println("count:" + zRange.size());
		System.out.println((System.nanoTime() - nanoTime) / 1000000);
	}


	// @Test
	public void testInsertToZWithTimeAndId() {
		RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
		for (int i = 0; i < 100000; i++) {
			Date d = new Date();
			Long time = d.getTime();

			String v = "{\"t\":" + time + ",\"id\":" + i + "}";
			// eval "redis.call('rpush', 'pointx', KEY[1])" 1
			String id = i + "";
			c.evalSha("343174adc1e047e9b8dd3aaf85dd114b368b251d", ReturnType.VALUE, 2, time.toString().getBytes(), id.getBytes());

			// deviceLocationRedisDeviceClient.opsForValue().set(time.toString(), v);
			c.set(id.getBytes(), v.getBytes());
		}
	}

	// @Test
	public void testZPollingAndCheckTime() throws IOException {
		RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
		long nanoTime = System.nanoTime();
		byte[] point = "pointx".getBytes();
		double d = 1421659909384d;
		Set<byte[]> zRangeByScore = c.zRangeByScore(point, 0, d);
		c.zRemRangeByScore(point, 0, d);
		byte[][] a = new byte[zRangeByScore.size()][];
		zRangeByScore.toArray(a);
		if (!zRangeByScore.isEmpty()) {
			Long del = c.del(a);
			System.out.println("count:" + del);
		}
		System.out.println((System.nanoTime() - nanoTime) / 1000000);
	}

	// @Test
	public void testInsertToListWithTimeAndId() throws IOException {
		RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
		for (int i = 0; i < 100000; i++) {
			Date d = new Date();
			long time = d.getTime();

			String v = "{\"t\":" + time + ",\"id\":" + i + "}";
			// eval "redis.call('rpush', 'pointx', KEY[1])" 1
			c.evalSha("109621841b8c60c3fea6f7240dc85dbfd71e8bae", ReturnType.VALUE, 1, v.getBytes());
			String id = i + "";
			deviceLocationRedisDeviceClient.opsForValue().set(id, v);
			// c.set(id.getBytes(), v.getBytes());
		}
	}

	// @Test
	public void testGetAllFromList() throws IOException, InterruptedException, ExecutionException {
		long nanoTime = System.nanoTime();
		ExecutorService tp = Executors.newFixedThreadPool(200);

		for (int i = 0; i < 500; i++) {
			Callable<Boolean> a = new Callable<Boolean>() {

				public Boolean call() throws Exception {
					final RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
					List<byte[]> lRange = c.lRange("pointx".getBytes(), 0L, -1L);

					for (byte[] s : lRange) {
						String v = new String(s);
						Map<String, Long> cm;
						try {
							cm = JsonUtils.convertJsonToMap(v, String.class, Long.class);
							cm.get("id");
						} catch (JsonFormatException e) {
							e.printStackTrace();
						}
					}
					System.out.println("size:" + lRange.size());
					return null;
				}
			};
			Future<Boolean> submit = tp.submit(a);
		}
		tp.shutdown();
		tp.awaitTermination(300, TimeUnit.SECONDS);
		System.out.println((System.nanoTime() - nanoTime) / 1000000);
	}

	// @Test
	public void testListPollingAndCheckTime() throws IOException {
		long count = 0;
		RedisConnection c = deviceLocationRedisDeviceClient.getConnectionFactory().getConnection();
		long nanoTime = System.nanoTime();
		byte[] point = "pointx".getBytes();
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		while (true) {
			byte[] lPop = c.lPop(point);
			String v = new String(lPop);
			Map<String, Long> cm = JsonUtils.convertJsonToMap(v, String.class, Long.class);
			if (cm.get("t") > 1421646522961L) {
				byte[][] a = new byte[list.size()][];
				list.toArray(a);
				c.del(a);
				count += list.size();
				list.clear();
				c.lPush(point, lPop);
				break;
			} else {
				Long id = cm.get("id");
				list.add(id.toString().getBytes());
				if (list.size() > 1000) {
					byte[][] a = new byte[list.size()][];
					list.toArray(a);
					c.del(a);
					count += list.size();
					list.clear();
				}

			}
		}
		System.out.println("size:" + count);
		System.out.println((System.nanoTime() - nanoTime) / 1000000);
	}



	// @Test
	public void testDeviceInfo() throws IOException {
		String deviceid = "99101";
		String snsToken = UUID.randomUUID().toString();
		Long point = 101000L;
		DeviceInfoCacheBean bean = new DeviceInfoCacheBean();
//		bean.setPoint(point);
		bean.setSnsToken(snsToken);
		bean.setDeviceNo(12L);
		deviceInfoCache.putDeviceInfo(deviceid, bean, DeviceInfoCacheKeyType.All);
		DeviceInfoCacheBean deviceInfo = deviceInfoCache.getDeviceInfo(deviceid);
		Assert.assertEquals(JsonUtils.convertObjectToJson(bean), JsonUtils.convertObjectToJson(deviceInfo));
	}

	// @Test
//	public void testDeviceLocation() {
//		String deviceid = "99101";
//		Long point = 101000L;
//		String snsToken = UUID.randomUUID().toString();
//
//		DeviceLocationCacheBean d = new DeviceLocationCacheBean();
//		d.setDeviceType(DeviceType.GCM);
//		d.setSnsToken(snsToken);
//		deviceLocationCache.putDeviceLocation(point.toString(), deviceid, d);
//
//		List<DeviceLocationCacheBean> deviceLocationByPoint = deviceLocationCache.getDeviceLocationByPoint(point.toString());
//		Assert.assertEquals(d.writeToCache(), deviceLocationByPoint.get(0).writeToCache());
//
//		DeviceLocationCacheBean deviceLocationByPointAndDeviceId =
//				deviceLocationCache.getDeviceLocationByPointAndDeviceId(point.toString(), deviceid);
//		Assert.assertEquals(d.writeToCache(), deviceLocationByPointAndDeviceId.writeToCache());
//
//		deviceLocationCache.delDeviceLocation(point.toString(), deviceid);
//		// deviceLocationByPointAndDeviceId =
//		// redis.getDeviceLocationByPointAndDeviceId(point.toString(), deviceid);
//		// Assert.fail();
//	}

	// @Test
	public void testMessage() throws IOException {
		BroMessageEntity me = new BroMessageEntity();
		me.setDeviceNo(99101L);
		me.setId(11111111L);
		me.setTitle("");
		me.setRadius(10);
		// me.setMessageType(MessageType.Clothing);
		me.setCreatetime(new Date());
		me.setContent("");
		me.setLatitude(1.0);
		me.setLongtitude(1.0);

		// redis.putMessage(me.getId(), me);
		// MessageEntity message = redis.getMessage(me.getId());
		// Assert.assertEquals(JsonUtils.convertObjectToJson(me),
		// JsonUtils.convertObjectToJson(message));
	}

	public static void main(String[] args) {
		ClassPathXmlApplicationContext a = new ClassPathXmlApplicationContext("classpath:cache-config.xml");
		a.start();
		JedisConnectionFactory b = (JedisConnectionFactory) a.getBean("jcf");
		// b.getConnection().getNativeConnection().psubscribe(new MySubscribe(),
		// "__keyevent@1__:expired");
	}
	//
	// public static class MySubscribe extends JedisPubSub {
	// @Override
	// public void onMessage(String arg0, String arg1) {
	// }
	//
	// @Override
	// public void onPUnsubscribe(String arg0, int arg1) {
	// }
	//
	// @Override
	// public void onSubscribe(String arg0, int arg1) {
	// }
	//
	// @Override
	// public void onUnsubscribe(String arg0, int arg1) {
	// }
	//
	// @Override
	// public void onPMessage(String arg0, String arg1, String arg2) {
	// System.out.println(arg0 + " " + arg1 + " " + arg2 );
	// }
	//
	// @Override
	// public void onPSubscribe(String arg0, int arg1) {
	// }
	//
	// }

}
