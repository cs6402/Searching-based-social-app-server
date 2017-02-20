package com.tdsoft.bro.msgcenter.ctrl;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.auth0.jwt.JWTVerifier;
import com.google.common.base.Optional;
import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.exception.DeviceNotFoundException;
import com.tdsoft.bro.core.oauth.JWTConstant;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;

public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {
	@Resource(name = "deviceInfoCacheImpl")
	private DeviceInfoCache cacheService;

	private JWTVerifier jwtVerifier;

	@Value("${jwt_key}")
	String jwtKey;

	@PostConstruct
	public void initialize() {
		jwtVerifier = new JWTVerifier(jwtKey.getBytes());
	}


	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {

//		Map<String, Object> verify = extractToken(request);
//		String deviceId = (String) verify.get(JWTConstant.ISS);
//		if (StringUtils.isBlank(deviceId)) {
//			throw new DeviceNotFoundException("Could not found device with empty device id");
//		}

		// retrieve device info from local cache
//		DeviceInfoCacheBean deviceInfo = cacheService.getDeviceInfo(deviceId);
//		if (!Optional.fromNullable(deviceInfo).isPresent()) {
//			throw new DeviceNotFoundException("Could not found device:" + deviceId);
//		}
//
//		ContextUtils.setDeviceInfo(deviceInfo);
		System.out.println("Before Handshake");
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
		System.out.println("After Handshake");
		super.afterHandshake(request, response, wsHandler, ex);
	}

	private Map<String, Object> extractToken(ServerHttpRequest request) {
		String token = getJWTString(request);
		try {
			return jwtVerifier.verify(token);
		} catch (Exception ex) {
			throw new DeviceNotFoundException(ex.getMessage());
		}
	}

	private String getJWTString(ServerHttpRequest httpRequest) {
		String token = null;
		List<String> list = httpRequest.getHeaders().get(HttpHeaders.AUTHORIZATION);
		String authorizationHeader = list.get(0);
		if (authorizationHeader == null) {
			throw new DeviceNotFoundException("Unauthorized: No Authorization header was found");
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
