package com.tdsoft.bro.qhandler.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tdsoft.bro.common.util.LocationUtils;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.bean.TagCSSearchBean;
import com.tdsoft.bro.core.bean.TagESSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.dao.DeviceTagDao;
import com.tdsoft.bro.core.entity.DeviceTagEntity;
import com.tdsoft.bro.core.qmsg.UploadTagQMsg;
import com.tdsoft.bro.core.service.cache.TagCache;
import com.tdsoft.bro.core.service.search.impl.TagElasticSearchImpl;
import com.tdsoft.bro.qhandler.service.ITagService;

@Service("tagServiceImpl")
public class TagServiceImpl implements ITagService {

	@Resource(name = "tagCacheImpl")
	TagCache cacheImpl;

	@Resource(name = "deviceTagDao")
	DeviceTagDao deviceTagDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void consumeTag(UploadTagQMsg qmsg, String body) {
		double[] pointXY = LocationUtils.convertLatLonToMercator(qmsg.getLatitude(), qmsg.getLongtitude());
		long point = LocationUtils.convertMecratorToPoint(pointXY[0], pointXY[1]);
		
		TagSearchBean bean = getESSearchBean(qmsg, body, point);
		
		TagCacheBean cacheBean = new TagCacheBean();
		cacheBean.setField(bean);
		cacheBean.setId(String.valueOf(qmsg.getDeviceNo()));
		cacheImpl.putTag(cacheBean);
		
		DeviceTagEntity entity = new DeviceTagEntity();
		entity.setDeviceNo(qmsg.getDeviceNo());
		entity.setLatitude(qmsg.getLatitude());
		entity.setLongtitude(qmsg.getLongtitude());
		entity.setTags(qmsg.getTags());
		entity.setLastRepostTime(new Date());
		deviceTagDao.save(entity);
	}
	
	/**
	 * For ElasticSearch data structure
	 */
	private TagSearchBean getESSearchBean(UploadTagQMsg qmsg, String body, long point) {
		TagESSearchBean bean = new TagESSearchBean();
		bean.setDeviceId(qmsg.getDeviceId());
		bean.setTags(qmsg.getTags());
		bean.setDeviceType(qmsg.getDeviceType());
		bean.setSnsToken(qmsg.getSnsToken());
		bean.setPoint(point);
		bean.getLocation().put(TagElasticSearchImpl.RESULT_FIELD_LAT, qmsg.getLatitude());
		bean.getLocation().put(TagElasticSearchImpl.RESULT_FIELD_LON, qmsg.getLongtitude());
		bean.setLatitude(qmsg.getLatitude());
		bean.setLongtitude(qmsg.getLongtitude());
		bean.setAliasName(qmsg.getAliasName());
		bean.setImage(qmsg.getImage());
		return bean;
	}
	
	/**
	 * For CloudSearch data structure
	 */
	@SuppressWarnings("unused")
	private TagSearchBean getCSSearchBean(UploadTagQMsg qmsg, String body, long point) {
		TagCSSearchBean bean = new TagCSSearchBean();
		bean.setDeviceId(qmsg.getDeviceId());
		bean.setLocation(new StringBuilder().append(qmsg.getLatitude()).append(",").append(qmsg.getLongtitude()).toString());
		bean.setTags(qmsg.getTags());
		bean.setDeviceType(qmsg.getDeviceType());
		bean.setSnsToken(qmsg.getSnsToken());
		bean.setPoint(point);
		bean.setLatitude(qmsg.getLatitude());
		bean.setLongtitude(qmsg.getLongtitude());
		bean.setAliasName(qmsg.getAliasName());
		bean.setImage(qmsg.getImage());
		return bean;
	}

}
