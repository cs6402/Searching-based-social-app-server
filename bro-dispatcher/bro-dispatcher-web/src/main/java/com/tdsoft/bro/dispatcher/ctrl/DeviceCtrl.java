package com.tdsoft.bro.dispatcher.ctrl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.auth0.jwt.JWTSigner;
import com.google.common.base.Optional;
import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.common.util.StringBuilderUtils;
import com.tdsoft.bro.core.bean.AliasBean;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;
import com.tdsoft.bro.core.exception.ArgumentInvalidException;
import com.tdsoft.bro.core.manager.AliasManager;
import com.tdsoft.bro.core.oauth.JWTConstant;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.dispatcher.dto.DeviceRefreshDTO;
import com.tdsoft.bro.dispatcher.dto.DeviceRegisterDTO;
import com.tdsoft.bro.dispatcher.dto.DeviceRegisterSuccessDTO;
import com.tdsoft.bro.dispatcher.dto.LoginDeviceDTO;
import com.tdsoft.bro.dispatcher.dto.UpdLocationDTO;
import com.tdsoft.bro.dispatcher.service.IDeviceService;

@Controller
@RequestMapping(value = "${ctrl_device_main}", consumes = MediaType.APPLICATION_JSON_VALUE)
public class DeviceCtrl {
	private static final Logger logger = LoggerFactory.getLogger(DeviceCtrl.class);
	@Resource(name = "deviceServiceImpl")
	IDeviceService deviceService;

	@Resource(name = "deviceInfoCacheImpl")
	DeviceInfoCache deviceInfoCache;

	@Resource(name = "aliasManager")
	AliasManager aliasManager;

	@Value("${jwt_key}")
	String jwtKey;

	boolean isDebugEnabled = logger.isDebugEnabled();
	
	/**
	 * 裝置註冊
	 * 
	 * @param DeviceRegisterDTO
	 * @return DeviceRegisterSuccessDTO
	 */
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> registerDevice(@Valid @RequestBody DeviceRegisterDTO dto, HttpServletRequest request,
			HttpServletResponse response) {
		StringBuilder deviceIdAssembler = new StringBuilder();
		// UUID : GCM Id, Apple Id, BAIDU UserId
		// 檢查UUID是否為空
		if (StringUtils.isBlank(dto.getUuid())) {
			// 隨機生成UUID
			dto.setUuid(UUID.randomUUID().toString());
			// 組合UUID : id@DeviceType, e.g 1138819804752832943@B
			deviceIdAssembler.append(dto.getUuid()).append(deviceInfoCache.getDeviceIdSeparator()).append(dto.getType());
		} else {
			// 組合UUID : id@DeviceType, e.g 1138819804752832943@B
			// 並檢查此ID是否存在
			DeviceInfoCacheBean deviceInfo = deviceInfoCache.getDeviceInfo(deviceIdAssembler.append(dto.getUuid())
					.append(deviceInfoCache.getDeviceIdSeparator()).append(dto.getType()).toString());

			if (Optional.fromNullable(deviceInfo).isPresent()) {
				// 發現存在cache，基本上為重新註冊(移除app後重新安裝)
				String dtoAsString = JsonUtils.convertObjectToJson(dto);
				String deviceInfoAsString = JsonUtils.convertObjectToJson(deviceInfo);

				// 比對cache中的裝置型態是否相同，相同則返回原snsToken與deviceId
				logger.info("Already register! Maybe reinstall app, dto:{} and device info cache:{}", dtoAsString, deviceInfoAsString);
				DeviceRegisterSuccessDTO resp = new DeviceRegisterSuccessDTO();
				resp.setDeviceId(deviceInfo.getDeviceId());
				resp.setSnsToken(deviceInfo.getSnsToken());
				resp.setAliasName(deviceInfo.getAliasName());
				resp.setImage(deviceInfo.getImage());
				// 防止會話劫持 重建SESSION
				// HttpSession session = preventSessionFixation(request, request.getSession());
				// if
				// (!Optional.fromNullable(session.getAttribute(SessionAttributeConstant.DEVICE_INFO_ID)).isPresent())
				// {
				// session.setAttribute(SessionAttributeConstant.DEVICE_INFO_ID,
				// deviceInfo.getDeviceId());
				// }
				addJWTToken(response, deviceInfo.getDeviceId());
				return new ResponseEntity<>(resp, HttpStatus.OK);
			}
		}

		// 裝置註冊
		DeviceInfoEntity entity = new DeviceInfoEntity();
		entity.setDeviceId(deviceIdAssembler.toString());
		entity.setUuid(dto.getUuid());
		entity.setToken(dto.getToken());
		entity.setType(dto.getType());
		AliasBean alias = aliasManager.getAlias(dto.getLang());
		entity.setAliasName(alias.getName());
		entity.setImage(alias.getImage());
		deviceService.registerDevice(entity, dto.getLatitude(), dto.getLongtitude());

		DeviceRegisterSuccessDTO resp = new DeviceRegisterSuccessDTO();
		resp.setDeviceId(entity.getDeviceId());
		resp.setSnsToken(entity.getSnsToken());
		resp.setAliasName(entity.getAliasName());
		resp.setImage(entity.getImage());
		addJWTToken(response, entity.getDeviceId());
		logger.info("Register device succeed! Id:{}, UUID:{}, Token:{}, SNS Token:{} ,Type:{}, Alias:{}, Image:{}", entity.getDeviceId(),
				entity.getUuid(), entity.getSnsToken(), entity.getType(), entity.getAliasName(), entity.getImage());
		// 防止會話劫持 重建SESSION
		// HttpSession session = preventSessionFixation(request, request.getSession());
		// session.setAttribute(SessionAttributeConstant.DEVICE_INFO_ID, entity.getDeviceId());
		return new ResponseEntity<>(resp, HttpStatus.CREATED);
	}

	/**
	 * 更新裝置推播資訊
	 * 
	 * @param deviceId
	 * @return
	 */
	@RequestMapping(value = "${ctrl_device_with_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> refreshDevice(@PathVariable("deviceId") String deviceId, @Valid @RequestBody DeviceRefreshDTO dto) {
		DeviceInfoCacheBean deviceInfo = ContextUtils.getDeviceInfo();
		// 裝置重註冊
		DeviceInfoEntity entity = new DeviceInfoEntity();
		entity.setDeviceId(deviceId);
		entity.setUuid(deviceInfo.getUuid());
		entity.setToken(dto.getToken());
		entity.setType(dto.getType());
		entity.setSnsToken(deviceInfo.getSnsToken());
		entity.setDeviceNo(deviceInfo.getDeviceNo());
		deviceService.refreshDevice(entity);

		DeviceRegisterSuccessDTO resp = new DeviceRegisterSuccessDTO();
		resp.setDeviceId(entity.getDeviceId());
		resp.setSnsToken(entity.getSnsToken());
		return new ResponseEntity<>(resp, HttpStatus.OK);
	}

	/**
	 * 更新裝置位置
	 * 
	 * @param deviceId
	 * @param location
	 * @param die
	 * @return
	 */
	@RequestMapping(value = "${ctrl_device_update_location}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> updateLocation(@PathVariable("deviceId") String deviceId, @Valid @RequestBody UpdLocationDTO dto) {
		DeviceInfoCacheBean info = ContextUtils.getDeviceInfo();
		deviceService.updateLocation(info, dto.getLatitude(), dto.getLongtitude());
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	/**
	 * 裝置登入
	 * 
	 * @param deviceId
	 * @param location
	 * @param die
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> login(@Valid @RequestBody LoginDeviceDTO dto, HttpServletResponse response) {

		// 並檢查此ID是否存在
		DeviceInfoCacheBean deviceInfo = deviceInfoCache.getDeviceInfo(dto.getDeviceId());
		if (Optional.fromNullable(deviceInfo).isPresent()) {
			if (deviceInfo.getSnsToken().equals(dto.getSnsToken())) {
				String sign = addJWTToken(response, deviceInfo.getDeviceId());
				if (isDebugEnabled) {
					 String dtoAsString = JsonUtils.convertObjectToJson(dto);
					 logger.debug("Login succeed, login info: {}, login data:{}", dtoAsString, sign);
				}
				return new ResponseEntity<String>(HttpStatus.OK);
			}
		}
		throw new ArgumentInvalidException("Login failed!");
	}

	private String addJWTToken(HttpServletResponse response, String deviceId) {
		Map<String, Object> claims = new HashMap<String, Object>();
		claims.put(JWTConstant.ISS, deviceId);

		claims.put(JWTConstant.EXP, Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli() / 1000L);
		claims.put(JWTConstant.JTI, UUID.randomUUID().toString());
		claims.put(JWTConstant.SUB, "Mobile");
		String sign = jwtSigner.sign(claims);
		Cookie aa = new Cookie(HttpHeaders.AUTHORIZATION, StringBuilderUtils.getStringBuilder().append("Bearer ").append(sign).toString());
		aa.setHttpOnly(true);
		aa.setPath("/");
		// aa.setSecure(true);
		response.addHeader(HttpHeaders.AUTHORIZATION, aa.getValue());
		response.addCookie(aa);
		return sign;
	}

	private JWTSigner jwtSigner;

	@PostConstruct
	public void initialize() {
		jwtSigner = new JWTSigner(jwtKey);
	}

	/**
	 * 裝置驗證
	 * 
	 * @param deviceId 裝置代號
	 * @param validationToken 驗證令牌
	 */
	@RequestMapping(value = "${ctrl_device_with_id}", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public void validateDevice(@PathVariable("deviceId") String deviceId, @Valid @RequestBody String validationToken) {
		deviceService.validateDevice(deviceId, validationToken);
	}
}
