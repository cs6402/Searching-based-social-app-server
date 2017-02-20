package com.tdsoft.bro.qhandler.service;

import com.tdsoft.bro.core.qmsg.UploadTagQMsg;

public interface ITagService {

	void consumeTag(UploadTagQMsg qmsg, String body);
}
