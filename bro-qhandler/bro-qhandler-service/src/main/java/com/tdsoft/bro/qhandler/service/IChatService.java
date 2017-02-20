package com.tdsoft.bro.qhandler.service;

import com.tdsoft.bro.core.qmsg.ChatQMsg;

public interface IChatService {
	void chat(String messageId, ChatQMsg qmsg, String body);
	void clean(Long timestamp);
}
