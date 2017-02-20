package com.tdsoft.bro.msgcenter.ctrl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.msgcenter.dto.ChatMessageDTO;

@Controller
public class MessageBroker extends TextWebSocketHandler {
	@Autowired
	MessageCtrl ctrl;

	@Autowired
	RequestMappingHandlerMapping e;

	
	
	private Map<String, WebSocketSession> webSocketSessions;
	// @PostConstruct
	// private void init() {
	// MultiKeyMap multiKey = new MultiKeyMap();
	// Map<RequestMappingInfo, HandlerMethod> handlerMethods = e.getHandlerMethods();
	// handlerMethods.forEach((k, v) -> {
	// if (v.getBeanType() == ctrl.getClass()) {
	// for (Method m : ctrl.getClass().getDeclaredMethods()) {
	// if (v.getMethod() == m) {
	// String name = k.getName();
	// Iterator<RequestMethod> iterator = k.getMethodsCondition().getMethods().iterator();
	// while (iterator.hasNext()) {
	// RequestMethod requestMethod = iterator.next();
	// multiKey.put(name, requestMethod.toString(), k.getParamsCondition().getExpressions().size());
	// }
	// }
	// }
	// }
	// });
	// }

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		if (webSocketSessions == null) {
			webSocketSessions = new ConcurrentHashMap<>();
			webSocketSessions.put(session.getId(), session);
		}
		
		WebSocketSession webSocketSession = webSocketSessions.get(session.getId());
		webSocketSession.sendMessage(message);
//		super.handleTextMessage(session, message);
//		String payload = message.getPayload();
//		WSBuffer wsBuffer = JsonUtils.convertJsonToObject(payload, WSBuffer.class);
////		String path = wsBuffer.getPath();
//		extracted(session, wsBuffer);
	}

//	private void extracted(WebSocketSession session, WSBuffer wsBuffer) throws IOException {
//		List<Object> content = JsonUtils.convertJsonToList(wsBuffer.getPayload(), Object.class);
//		ResponseEntity<List<ChatMessageDTO>> retrieveMessages =
//				ctrl.retrieveMessages(content.get(0).toString(), Long.valueOf(content.get(1).toString()),
//						Long.valueOf(content.get(2).toString()));
//		// ResponseEntity<List<DeviceMessageCacheBean>> retrieveMessages =
//		// ctrl.retrieveMessages("889526150415181080@B", 0L, 1l);
//		TextMessage returnMessage = new TextMessage(JsonUtils.convertObjectToJson(retrieveMessages));
//
//		session.sendMessage(returnMessage);
//	}
}
