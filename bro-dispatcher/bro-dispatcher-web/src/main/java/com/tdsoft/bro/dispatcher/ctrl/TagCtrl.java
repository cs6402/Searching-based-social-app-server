package com.tdsoft.bro.dispatcher.ctrl;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tdsoft.bro.common.util.ContextUtils;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.qmsg.UploadTagQMsg;
import com.tdsoft.bro.dispatcher.service.ITagService;

@Controller
public class TagCtrl {
	@Resource(name = "tagServiceImpl")
	private ITagService tagService;

	@RequestMapping(value = "${ctrl_tag_main}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> postTags(@Valid @RequestBody List<String> dtos) {
		DeviceInfoCacheBean deviceInfo = ContextUtils.getDeviceInfo();
		UploadTagQMsg qmsg = new UploadTagQMsg();
		qmsg.setDeviceId(deviceInfo.getDeviceId());
		qmsg.setLongtitude(deviceInfo.getLongtitude());
		qmsg.setLatitude(deviceInfo.getLatitude());
		qmsg.setDeviceNo(deviceInfo.getDeviceNo());
		qmsg.setSnsToken(deviceInfo.getSnsToken());
		qmsg.setDeviceType(deviceInfo.getType());
		qmsg.setAliasName(deviceInfo.getAliasName());
		qmsg.setImage(deviceInfo.getImage());
		String[] tags = new String[dtos.size()];
		dtos.toArray(tags);
		qmsg.setTags(tags);
		tagService.postTags(qmsg);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
