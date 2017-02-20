package com.tdsoft.bro.dispatcher.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.common.base.Objects;
import com.tdsoft.bro.common.DeviceInfoCacheKeyType;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.dao.DeviceInfoDao;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;
import com.tdsoft.bro.core.exception.ArgumentInvalidException;
import com.tdsoft.bro.core.exception.BroException;
import com.tdsoft.bro.core.exception.DeviceNotFoundException;
import com.tdsoft.bro.core.qmsg.InsertDeviceQMsg;
import com.tdsoft.bro.core.qmsg.UpdateLocationQMsg;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.DeviceLocationCache;
import com.tdsoft.bro.dispatcher.service.IDeviceService;

@Service("deviceServiceImpl")
public class DeviceServiceImpl implements IDeviceService {
	private static final Logger logger = LoggerFactory.getLogger(DeviceServiceImpl.class);

	@Resource(name = "amazonSNSClient")
	private AmazonSNS snsService;

	@Resource(name = "amazonSQSClient")
	private AmazonSQS sqsService;

	@Resource(name = "deviceInfoDao")
	private DeviceInfoDao deviceInfoDao;

	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache deviceInfoCache;

	@Resource(name = "deviceLocationCacheImpl")
	private DeviceLocationCache deviceLocationCache;
	/**
	 * 三種終端位置
	 */
	@Value("${service_device_android_platform_arn_prefix}")
	private String androidArn;
	@Value("${service_device_baidu_platform_arn_prefix}")
	private String baiduArn;
	@Value("${service_device_ios_platform_arn_prefix}")
	private String iosArn;

	/**
	 * 分割符號-三種終端位置加上斜線 切割與取出SNSToken
	 */
	@Value("${service_device_android_target_arn_delimiter}")
	private String androidArnDelimiter;
	@Value("${service_device_baidu_target_arn_delimiter}")
	private String baiduArnDelimiter;
	@Value("${service_device_ios_target_arn_delimiter}")
	private String iosArnDelimiter;

	@Value("${queue_device_update_location_url}")
	private String updateLocationQueueURL;

	@Value("${queue_device_insert_url}")
	private String insertDeviceQueueURL;

	private String registerSNS(String token, DeviceType type) {
		String arn = "";
		String delimiter = "";
		boolean isRegister = false;
		try {
			CreatePlatformEndpointRequest cper = new CreatePlatformEndpointRequest();
			switch (type) {
				case GCM:
					arn = androidArn;
					delimiter = androidArnDelimiter;
					break;
				case BAIDU:
					arn = baiduArn;
					delimiter = baiduArnDelimiter;
					Map<String, String> attributes = new HashMap<String, String>();
					String[] baiduTokens = token.split(",");
					if (!Objects.equal(baiduTokens.length, 2)) {
						throw new ArgumentInvalidException("baidu tokens error!");
					}
					attributes.put("UserId", baiduTokens[0]);
					attributes.put("ChannelId", baiduTokens[1]);
					cper.setAttributes(attributes);
					token = baiduTokens[1];
					break;
				case APNS:
					arn = iosArn;
					delimiter = iosArnDelimiter;
					break;
				default:
					throw new ArgumentInvalidException("DeviceType error!");
			}

			cper.setToken(token);
			cper.setPlatformApplicationArn(arn);
			CreatePlatformEndpointResult crt = snsService.createPlatformEndpoint(cper);
			isRegister = true;
			arn = crt.getEndpointArn();
			String[] stringArray = crt.getEndpointArn().split(delimiter);

			if (!Objects.equal(stringArray.length, 2)) {
				StringBuilder sb = new StringBuilder();
				sb.append("DeviceType: ").append(type).append("Token: ").append(token);
				sb.append(" occured register endpoint error.");
				throw new BroException(sb.toString());
			}
			if (logger.isDebugEnabled())
				logger.debug("Register Deivce succeed! DeviceId: {}, device type: {}, token: {}", stringArray[1], type, token);
			return stringArray[1];
		} catch (RuntimeException e) {
			if (isRegister) {
				DeleteEndpointRequest de = new DeleteEndpointRequest();
				de.setEndpointArn(arn);
				snsService.deleteEndpoint(de);
			}
			throw e;
		}
	}

	@Override
	public void registerDevice(DeviceInfoEntity entity, double latitude, double longtitude) {
		String snsToken = registerSNS(entity.getToken(), entity.getType());
		entity.setSnsToken(snsToken);
		String vaildationToken = UUID.randomUUID().toString();
		String entityInJson = JsonUtils.convertObjectToJson(entity);

		StringBuilder sbArn = new StringBuilder();
		switch (entity.getType()) {
			case APNS:
				sbArn.append(iosArnDelimiter);
				break;
			case BAIDU:
				sbArn.append(baiduArnDelimiter);
				break;
			case GCM:
				sbArn.append(androidArnDelimiter);
				break;
			default:
		}
		PublishRequest pr = new PublishRequest();
		pr.setMessage(vaildationToken);
		pr.setTargetArn(sbArn.append(snsToken).toString());
		// format: "UUID,lat,lon,{entity json body}"
		deviceInfoCache.putDeviceValidation(entity.getDeviceId(), new StringBuilder().append(vaildationToken).append(",").append(latitude)
				.append(",").append(longtitude).append(",").append(entityInJson).toString());
		snsService.publish(pr);
	}

	@Override
	public void updateLocation(DeviceInfoCacheBean info, double latitude, double longtitude) {
		UpdateLocationQMsg qmsg = new UpdateLocationQMsg();
		qmsg.setDeviceId(info.getDeviceId());
		qmsg.setDeviceNo(info.getDeviceNo());
		qmsg.setDeviceType(info.getType());
		qmsg.setLatitude(latitude);
		qmsg.setLongtitude(longtitude);
		qmsg.setSnsToken(info.getSnsToken());
		qmsg.setOldLatitude(info.getLatitude());
		qmsg.setOldLongtitude(info.getLongtitude());
		SendMessageRequest smr = new SendMessageRequest();
		smr.setMessageBody(JsonUtils.convertObjectToJson(qmsg));
		smr.setQueueUrl(updateLocationQueueURL);
		sqsService.sendMessage(smr);
	}

	@Override
	@Transactional
	public void refreshDevice(DeviceInfoEntity entity) {
		StringBuilder sb = new StringBuilder();
		switch (entity.getType()) {
			case GCM:
				sb.append(androidArn);
				break;
			case BAIDU:
				sb.append(baiduArnDelimiter);
				break;
			case APNS:
				sb.append(iosArn);
				break;
			default:
				throw new ArgumentInvalidException("DeviceType error!");
		}
		sb.append(entity.getSnsToken());
		// 刪除SNS註冊
		DeleteEndpointRequest de = new DeleteEndpointRequest();
		de.setEndpointArn(sb.toString());
		snsService.deleteEndpoint(de);

		// 重新註冊sns
		String snsToken = registerSNS(entity.getToken(), entity.getType());
		entity.setSnsToken(snsToken);
		deviceInfoDao.updateSnsToken(entity.getDeviceNo(), entity.getSnsToken());

		// 更新cache
		DeviceInfoCacheBean dc = new DeviceInfoCacheBean();
		dc.setSnsToken(entity.getSnsToken());
		deviceInfoCache.putDeviceInfo(entity.getDeviceId(), dc, DeviceInfoCacheKeyType.SNS_TOKEN);
	}

	@Override
	public void validateDevice(String deviceId, String validationToken) {
		String deviceValidation = deviceInfoCache.getDeviceValidation(deviceId);
		if (StringUtils.isNotBlank(deviceValidation)) {
			// format: "UUID,lat,lon,{entity json body}"
			String[] split = deviceValidation.split(",", 4);
			if (split.length != 4) {
				logger.error("Unknown error! Could not extract validation token. DeviceId:{}, Token:{}", deviceId, deviceValidation);
				throw new BroException("Unknown error! Could not extract validation token.");
			}
			if (split[0].equals(validationToken)) {
				double latitude = Double.parseDouble(split[1]);
				double longtitude = Double.parseDouble(split[2]);
				DeviceInfoEntity entity = JsonUtils.convertJsonToObject(split[3], DeviceInfoEntity.class);
				// 由QHandler進行存入資料庫
				SendMessageRequest smr = new SendMessageRequest();
				InsertDeviceQMsg qmsg = new InsertDeviceQMsg();
				qmsg.setLongtitude(longtitude);
				qmsg.setLatitude(latitude);
				qmsg.setEntity(entity);
				smr.setMessageBody(JsonUtils.convertObjectToJson(qmsg));
				smr.setQueueUrl(insertDeviceQueueURL);
				sqsService.sendMessage(smr);
				// 放入Cache
				DeviceInfoCacheBean dc = new DeviceInfoCacheBean();
				dc.setDeviceId(entity.getDeviceId());
				dc.setSnsToken(entity.getSnsToken());
				dc.setType(entity.getType());
				dc.setUuid(entity.getUuid());
				dc.setLongtitude(longtitude);
				dc.setLatitude(latitude);
				dc.setAliasName(entity.getAliasName());
				dc.setImage(entity.getImage());
				deviceInfoCache.putDeviceInfo(entity.getDeviceId(), dc, DeviceInfoCacheKeyType.UUID, DeviceInfoCacheKeyType.LOC_LON,
						DeviceInfoCacheKeyType.LOC_LAT, DeviceInfoCacheKeyType.SNS_TOKEN, DeviceInfoCacheKeyType.TYPE,
						DeviceInfoCacheKeyType.ALIAS, DeviceInfoCacheKeyType.IMAGE);
				logger.info("Validate device succeed! Id:{}, UUID:{}, Token:{}, SNS Token:{} ,Type:{}, Alias:{}, Image:{}",
						entity.getDeviceId(), entity.getUuid(), entity.getSnsToken(), entity.getType(), entity.getAliasName(),
						entity.getImage());
				return;
			}
		}
		logger.warn("Validation toke is out of date. DeviceId:{}, Device received validation Token:{}", deviceId, validationToken);
		throw new DeviceNotFoundException("Validation toke is out of date");
	}
}
