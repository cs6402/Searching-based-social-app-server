package com.tdsoft.bro.core.listener;

import java.io.Serializable;
import java.util.Map;

public interface IMessageDelegate {
	void handleMessage(String message);

	void handleMessage(Map<?, ?> message);

	void handleMessage(byte[] message);

	void handleMessage(Serializable message);

	// pass the channel/pattern as well
	void handleMessage(Serializable message, String channel);
}
