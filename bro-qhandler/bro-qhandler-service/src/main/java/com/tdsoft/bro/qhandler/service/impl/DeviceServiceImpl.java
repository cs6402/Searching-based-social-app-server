package com.tdsoft.bro.qhandler.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.google.common.base.Optional;
import com.tdsoft.bro.common.DeviceInfoCacheKeyType;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.bean.DeviceLocationCacheBean;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.dao.DeviceInfoDao;
import com.tdsoft.bro.core.dao.DeviceLocationDao;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;
import com.tdsoft.bro.core.entity.DeviceLocationEntity;
import com.tdsoft.bro.core.qmsg.InsertDeviceQMsg;
import com.tdsoft.bro.core.qmsg.UpdateLocationQMsg;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.DeviceLocationCache;
import com.tdsoft.bro.core.service.cache.TagCache;
import com.tdsoft.bro.qhandler.service.IDeviceService;
import com.tdsoft.bro.qhandler.service.ITagService;

@Service("deviceServiceImpl")
public class DeviceServiceImpl implements IDeviceService {
	private static final Logger LOG = LoggerFactory.getLogger(DeviceServiceImpl.class);

	@Resource(name = "amazonSNSClient")
	private AmazonSNS snsService;

	@Resource(name = "deviceInfoCacheImpl")
	DeviceInfoCache deviceInfoCache;

	@Resource(name = "deviceLocationCacheImpl")
	DeviceLocationCache deviceLocationCache;

	@Resource(name = "deviceInfoDao")
	private DeviceInfoDao deviceInfoDao;

	@Resource(name = "deviceLocationDao")
	private DeviceLocationDao deviceLocationDao;
	
	@Resource(name = "tagServiceImpl")
	private ITagService tagServiceImpl;
	
	@Resource(name = "tagCacheImpl")
	private TagCache tagCache;

	/**
	 * 三種終端位置
	 */
	private String androidArn;
	private String baiduArn;
	private String iosArn;

	private boolean isDebugEnabled = LOG.isDebugEnabled();

	@Transactional(propagation = Propagation.REQUIRED)
	public void updateLocation(UpdateLocationQMsg qmsg) {
		// 1. update to db
		// 2. put to device location cache & remove old location cache
		// 3. put to device info cache
		// 4. redis execute
		DeviceLocationEntity deviceLocationEntity = new DeviceLocationEntity();
		deviceLocationEntity.setDeviceNo(qmsg.getDeviceNo());
		deviceLocationEntity.setLatitude(qmsg.getLatitude());
		deviceLocationEntity.setLongtitude(qmsg.getLongtitude());
		deviceLocationEntity.setLastRepostTime(new Date());
		deviceLocationDao.save(deviceLocationEntity);

		DeviceLocationCacheBean deviceLocationCacheBean = new DeviceLocationCacheBean();
		deviceLocationCacheBean.setDeviceType(qmsg.getDeviceType());
		deviceLocationCacheBean.setSnsToken(qmsg.getSnsToken());

		Long point = deviceLocationCache.putDeviceLocation(qmsg.getLatitude(), qmsg.getLongtitude(), qmsg.getDeviceId(), deviceLocationCacheBean);
		deviceLocationCache.delDeviceLocation(qmsg.getOldLatitude(), qmsg.getOldLongtitude(), qmsg.getDeviceId());

		DeviceInfoCacheBean deviceInfoCacheBean = new DeviceInfoCacheBean();
		deviceInfoCacheBean.setLongtitude(qmsg.getLongtitude());
		deviceInfoCacheBean.setLatitude(qmsg.getLatitude());
		deviceInfoCache.putDeviceInfo(qmsg.getDeviceId(), deviceInfoCacheBean, DeviceInfoCacheKeyType.LOC_LON,
				DeviceInfoCacheKeyType.LOC_LAT);
		
		TagCacheBean tag = tagCache.getTag(qmsg.getDeviceId());
		TagSearchBean field = tag.getField();
		field.setLatitude(qmsg.getLatitude());
		field.setLongtitude(qmsg.getLongtitude());
		field.setPoint(point);
		tagCache.putTag(tag);
		
		if (isDebugEnabled)
			LOG.debug("Device location update: {}", JsonUtils.convertObjectToJson(qmsg));
	}

	public void deleteSNSDevice(String snsToken, DeviceType type) {
		String arn = "";
		switch (type) {
			case GCM:
				arn = androidArn;
				break;
			case BAIDU:
				arn = baiduArn;
				break;
			case APNS:
				arn = iosArn;
				break;
			default:
				throw new IllegalArgumentException("DeviceType error!");
		}
		DeleteEndpointRequest der = new DeleteEndpointRequest();
		der.setEndpointArn(arn + snsToken);
		try {
			snsService.deleteEndpoint(der);
		} catch (Exception e) {
			LOG.error("Delete deivce's registration failed! DeviceId:" + snsToken + ", device type:" + type, e);
			return;
		}

		if (isDebugEnabled)
			LOG.debug("Delete deivce's registration succeed! DeviceId:" + snsToken + ", device type:" + type);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void insertDevice(InsertDeviceQMsg qmsg) {
		DeviceInfoEntity entity = qmsg.getEntity();
		// 利用裝置代號取得DeviceNo，檢驗是否已經存在該裝置
		Long deviceNo = deviceInfoDao.getDeviceNoByDeviceId(entity.getDeviceId());
		if (Optional.fromNullable(deviceNo).isPresent() && deviceNo != 0) {
			// 將DeviceNo放回Entity
			entity.setDeviceNo(deviceNo);
			LOG.warn("Device already exists.It means that one of the cluster node has crushed. Detail: {}",
					JsonUtils.convertObjectToJson(entity));
		} else {
			deviceInfoDao.save(entity);
		}
		// 補回DeviceNo給Cache
		DeviceInfoCacheBean dc = new DeviceInfoCacheBean();
		dc.setDeviceNo(entity.getDeviceNo());
		deviceInfoCache.putDeviceInfo(entity.getDeviceId(), dc, DeviceInfoCacheKeyType.NO);
		// 放入Location
		DeviceLocationCacheBean location = new DeviceLocationCacheBean();
		location.setDeviceType(entity.getType());
		location.setSnsToken(entity.getSnsToken());
		deviceLocationCache.putDeviceLocation(qmsg.getLatitude(), qmsg.getLongtitude(), entity.getDeviceId(), location);
		
		if (isDebugEnabled)
			LOG.debug("Device insertion succeed, Detail: {}", JsonUtils.convertObjectToJson(entity));
	}
}
