package com.tdsoft.bro.core.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.tdsoft.bro.common.util.ContextUtils;

/**
 * 使用於清除上下文資訊的攔截器
 * <h1>順序排最後</h1>
 * @author Daniel
 *
 */
public class ContextCleanInterceptor extends HandlerInterceptorAdapter {
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		ContextUtils.clean();
	}
}
