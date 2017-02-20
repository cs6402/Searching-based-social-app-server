package com.tdsoft.bro.qhandler.service;

import com.tdsoft.bro.core.qmsg.InsertDeviceQMsg;
import com.tdsoft.bro.core.qmsg.UpdateLocationQMsg;

public interface IDeviceService {

	void updateLocation(UpdateLocationQMsg qmsg);

	void insertDevice(InsertDeviceQMsg qmsg);
}
