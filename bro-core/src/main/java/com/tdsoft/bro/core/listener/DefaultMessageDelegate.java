package com.tdsoft.bro.core.listener;

import java.io.Serializable;
import java.util.Map;

public class DefaultMessageDelegate implements IMessageDelegate {

	public void handleMessage(String message) {}

	public void handleMessage(Map<?, ?> message) {}

	public void handleMessage(byte[] message) {}

	public void handleMessage(Serializable message) {}

	public void handleMessage(Serializable message, String channel) {}

}
