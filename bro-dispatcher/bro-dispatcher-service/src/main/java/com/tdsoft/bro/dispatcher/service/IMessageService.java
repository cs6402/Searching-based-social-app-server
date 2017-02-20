package com.tdsoft.bro.dispatcher.service;

import com.tdsoft.bro.core.qmsg.BroadcastQMsg;
import com.tdsoft.bro.core.qmsg.ChatQMsg;


public interface IMessageService {
	String broadcast(BroadcastQMsg qmsg);
	String chat(ChatQMsg m);
}
