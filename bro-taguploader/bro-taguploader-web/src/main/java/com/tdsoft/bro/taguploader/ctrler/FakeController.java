package com.tdsoft.bro.taguploader.ctrler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FakeController {
	
	@RequestMapping(value = "")
	public void sendFakeMsg2SQS() {
		
	}
}
