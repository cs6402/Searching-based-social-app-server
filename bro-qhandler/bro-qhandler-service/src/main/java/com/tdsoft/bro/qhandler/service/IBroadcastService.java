package com.tdsoft.bro.qhandler.service;

import com.tdsoft.bro.core.qmsg.BroadcastQMsg;

public interface IBroadcastService {

	void broadcast(String messageId, BroadcastQMsg msg, String body);

}
