package com.tdsoft.bro.core.interceptor;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import com.auth0.jwt.JWTVerifier;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.exception.DeviceNotFoundException;
import com.tdsoft.bro.core.oauth.JWTConstant;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;

public class DeviceVerifyInterceptor extends HandlerInterceptorAdapter {
	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache cacheService;

	/**
	 * 逾時時間設定
	 */
	private int timeout = 300;

//	private Cache<String, DeviceInfoCacheBean> localCache;

	private JWTVerifier jwtVerifier;

	@Value("${jwt_key}")
	String jwtKey;
	
	@PostConstruct
	public void initialize() {
//		localCache = CacheBuilder.newBuilder().expireAfterAccess(timeout, TimeUnit.SECONDS).maximumSize(1000).build();
		jwtVerifier = new JWTVerifier(jwtKey.getBytes());
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Map<String, Object> verify = extractToken(request);
		String deviceId = (String) verify.get(JWTConstant.ISS);
		if (StringUtils.isBlank(deviceId)) {
			throw new DeviceNotFoundException("Could not found device with empty device id");
		}

		// first retrieve device info from local cache
//		DeviceInfoCacheBean deviceInfo = localCache.getIfPresent(deviceId);
//		if (!Optional.fromNullable(deviceInfo).isPresent()) {
			// end up with retrieve from remote cache
		DeviceInfoCacheBean deviceInfo = cacheService.getDeviceInfo(deviceId);
			if (!Optional.fromNullable(deviceInfo).isPresent()) {
				throw new DeviceNotFoundException("Could not found device:" + deviceId);
			}
//			localCache.put(deviceId, deviceInfo);
//		}
		ContextUtils.setDeviceInfo(deviceInfo);
		return true;
	}

	private Map<String, Object> extractToken(HttpServletRequest request) {
		String token = getJWTString(request);
		try {
			return jwtVerifier.verify(token);
		} catch (Exception ex) {
			throw new DeviceNotFoundException(ex.getMessage());
		}
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private String getJWTString(HttpServletRequest httpRequest) {
		String token = null;
		String authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorizationHeader == null) {
			Cookie cookie = WebUtils.getCookie(httpRequest, HttpHeaders.AUTHORIZATION);
			if (cookie == null) {
				throw new DeviceNotFoundException("Unauthorized: No Authorization header was found");
			}
			authorizationHeader = cookie.getValue();
		}

		String[] parts = authorizationHeader.split(" ");
		if (parts.length != 2) {
			throw new DeviceNotFoundException("Unauthorized: Format is Authorization: Bearer [token]");
		}

		String scheme = parts[0];
		String credentials = parts[1];

		Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
		if (pattern.matcher(scheme).matches()) {
			token = credentials;
		}
		return token;
	}

}
