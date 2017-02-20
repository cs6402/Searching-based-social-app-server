package com.tdsoft.bro.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/error")
public class HttpErrorPresentCtrl {

	@RequestMapping(value = "/404")
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String error404() {
		return "";
	}
}
