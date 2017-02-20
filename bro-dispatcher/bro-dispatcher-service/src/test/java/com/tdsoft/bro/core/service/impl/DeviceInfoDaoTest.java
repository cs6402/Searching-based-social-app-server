package com.tdsoft.bro.core.service.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.core.dao.DeviceInfoDao;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:test-config.xml"})
public class DeviceInfoDaoTest {
	@Autowired
	DeviceInfoDao dao;

	@Ignore
	@Test(expected = DuplicateKeyException.class)
	public void testInsert() {
		DeviceInfoEntity d = new DeviceInfoEntity();
		d.setDeviceNo(5l);
		d.setDeviceId("1138819804752832943@C");
		dao.save(d);
	}

	@Test
	public void testPaging() {
		PageRequest r = new PageRequest(0, 1);
		Page<DeviceInfoEntity> streamAllPaged = getAll(r);

//		List<DeviceInfoEntity> findAll = dao.findAll();
//		findAll.forEach(e -> {
//			System.err.println("DD" + e);
//		});
	}

	private Page<DeviceInfoEntity> getAll(Pageable pageable) {
		Page<DeviceInfoEntity> streamAllPaged = dao.findAll(pageable);
		streamAllPaged.getContent().forEach(e -> {
			System.err.println(e);
		});
		if (streamAllPaged.hasNext()){
			getAll(streamAllPaged.nextPageable());
		}
		return streamAllPaged;
	}
	
	
}
