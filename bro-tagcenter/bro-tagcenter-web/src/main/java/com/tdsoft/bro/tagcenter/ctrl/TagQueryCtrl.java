package com.tdsoft.bro.tagcenter.ctrl;

import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tdsoft.bro.core.bean.TagSearchResultBean;
import com.tdsoft.bro.core.manager.SuggestionManager;
import com.tdsoft.bro.core.service.search.TagSearch;

@Controller
public class TagQueryCtrl {
	@Resource(name = "tagSearchImpl")
	private TagSearch searchService;
	@Resource(name = "suggestionManager")
	private SuggestionManager sm;

	@RequestMapping(value = "${ctrl_tag_retrieve}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> retrieveNearTags(@Valid @DecimalMax("90") @DecimalMin("-90") @PathVariable("latitude") Double latitude,
			@Valid @DecimalMax("180") @DecimalMin("-180") @PathVariable("longtitude") Double longtitude,
			@Valid @DecimalMin("1") @DecimalMax("100") @PathVariable("page") Long page) {
		List<TagSearchResultBean> retrieveNearTags = searchService.retrieveNearTags(latitude, longtitude, page);
		return new ResponseEntity<List<TagSearchResultBean>>(retrieveNearTags, HttpStatus.OK);
	}

	@RequestMapping(value = "${ctrl_tag_query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> queryTag(@Valid @DecimalMax("90") @DecimalMin("-90") @PathVariable("latitude") Double latitude,
			@Valid @DecimalMax("180") @DecimalMin("-180") @PathVariable("longtitude") Double longtitude,
			@PathVariable("keyword") String keyword, @Valid @DecimalMin("1") @DecimalMax("100") @PathVariable("page") Long page) {
		List<TagSearchResultBean> searchTags = searchService.searchTags(latitude, longtitude, keyword, page);
		return new ResponseEntity<List<TagSearchResultBean>>(searchTags, HttpStatus.OK);
	}

	@RequestMapping(value = "${ctrl_tag_top10_suggestion}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> queryTop10Suggestion(@Valid @PathVariable("lang") String lang) {
		String suggestions = sm.getSuggestions(lang);
		return new ResponseEntity<String>(suggestions, HttpStatus.OK);
	}
}
