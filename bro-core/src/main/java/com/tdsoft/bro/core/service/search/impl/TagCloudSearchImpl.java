package com.tdsoft.bro.core.service.search.impl;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.amazonaws.services.cloudsearchdomain.AmazonCloudSearchDomain;
import com.amazonaws.services.cloudsearchdomain.model.ContentType;
import com.amazonaws.services.cloudsearchdomain.model.Hit;
import com.amazonaws.services.cloudsearchdomain.model.QueryParser;
import com.amazonaws.services.cloudsearchdomain.model.SearchRequest;
import com.amazonaws.services.cloudsearchdomain.model.SearchResult;
import com.amazonaws.services.cloudsearchdomain.model.UploadDocumentsRequest;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.core.bean.TagCSSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.bean.TagSearchResultBean;
import com.tdsoft.bro.core.service.search.TagSearch;

//@Service("tagSearchImpl")
/**
 * TagCloudSearchBean ��閬��ocation 甈��at, Lon 蝯���葡 嚗� e.g "25.08,121.13"
 * @author Daniel
 *
 */
@Deprecated
public class TagCloudSearchImpl implements TagSearch {
	
	@Resource(name = "tagSearchClient")
	AmazonCloudSearchDomain tagSearchClient;
	
	private static final String TERM_SEARCH_TAGS = "(and tags:'%s')";
	private static final String RETUREN_NEAR_DEVICE_INFO = "I,distance,TS,LOC,A,IMG";
	private static final String RETUREN_NEAR_DEVICE_ID = "I,distance,S";

	private static final String DISTANCE_EXPRESSION = "{distance:'haversin(%s,%s,location.latitude,location.longitude)'}";
	private static final String SORT_DISTANCE = "distance asc";
	
	private static final String RESULT_FIELD_DEVICE_ID = "I";
	private static final String RESULT_FIELD_LOCATION = "LOC";
	private static final String RESULT_FIELD_TAGS="TS";
	private static final String RESULT_FIELD_DISTANCE = "distance";
	private static final String RESULT_FIELD_SNS_TOKEN = "S";
	private static final String RESULT_FIELD_DEVICE_TYPE = "T";
	private static final String RESULT_FIELD_POINT = "P";
	private static final String RESULT_FIELD_ALIAS = "A";
	private static final String RESULT_FIELD_IMAGE = "IMG";
	private String nearSearchPattern = "";
	
	@PostConstruct
	private void init() {
		// ��������摰� e.g (or device_type:'GCM'(or device_type:'APNS'(or device_type:'BAIDU')))
		StringBuilder sb = new StringBuilder();
		StringBuilder ender = new StringBuilder();
		for (DeviceType type : DeviceType.values()) {
			sb.append("(or device_type:'").append(type.name()).append("'");
			ender.append(")");
		}
		sb.append(ender.toString());
		nearSearchPattern = sb.toString();
	}
	
	@Override
	public void postTags(byte[] documents, List<String> allDoc) {
		UploadDocumentsRequest uploadDocumentsRequest = new UploadDocumentsRequest();
		uploadDocumentsRequest.setDocuments(new ByteArrayInputStream(documents));
		uploadDocumentsRequest.setContentType(ContentType.Applicationjson);
		uploadDocumentsRequest.setContentLength((long) documents.length);
		tagSearchClient.uploadDocuments(uploadDocumentsRequest);
	}

	@Override
	public List<TagSearchResultBean> retrieveNearTags(Double latitude, Double longtitude, Long page) {
//		String s = String.format("(and location :['%s, %s', '%s,%s'])", nf.format(latitude.doubleValue() + 0.1), nf.format(longtitude.doubleValue() - 0.1), nf.format(latitude.doubleValue() - 0.1), nf.format(longtitude.doubleValue() + 0.1));
		String expression = String.format(DISTANCE_EXPRESSION, latitude, longtitude);
		SearchRequest req = new SearchRequest();
		req.setQuery(nearSearchPattern);
		req.setExpr(expression);
		req.setSort(SORT_DISTANCE);
		req.setQueryParser(QueryParser.Structured);
		req.setStart(10 * (page - 1));
		req.setReturn(RETUREN_NEAR_DEVICE_INFO);
		SearchResult search = tagSearchClient.search(req);
		List<Hit> hits = search.getHits().getHit();
		List<TagSearchResultBean> tags = new ArrayList<>();
		for (Hit h : hits) {
			try {
				TagSearchResultBean bean = filloutTagSearchResultBean(h);
				tags.add(bean);
			} catch (Exception e) {
				// silently
			}
		}
		return tags;
	}

	@Override
	public List<TagSearchResultBean> searchTags(Double latitude, Double longtitude, String keyword, Long page) {
		SearchRequest req = new SearchRequest();
		String searchTerm = String.format(TERM_SEARCH_TAGS, keyword);
		req.setQuery(searchTerm);
		String expression = String.format(DISTANCE_EXPRESSION, latitude, longtitude);
		req.setExpr(expression);
		req.setSort(SORT_DISTANCE);
		req.setQueryParser(QueryParser.Structured);
		req.setStart(10 * (page - 1));
		req.setReturn(RETUREN_NEAR_DEVICE_INFO);
		SearchResult search = tagSearchClient.search(req);
		List<Hit> hits = search.getHits().getHit();
		List<TagSearchResultBean> tags = new ArrayList<>();
		for (Hit h : hits) {
			try {
				TagSearchResultBean bean = filloutTagSearchResultBean(h);
				tags.add(bean);
			} catch (Exception e) {
				// silently
			}
		}
		return tags;
	}
	
	@Override
	public List<SimpleEntry<String, String>> retrieveNearDeviceId(Double latitude, Double longtitude) {
		String expression = String.format(DISTANCE_EXPRESSION, latitude, longtitude);
		SearchRequest req = new SearchRequest();
		req.setQuery(nearSearchPattern);
		req.setQueryParser(QueryParser.Structured);
		req.setExpr(expression);
		req.setSort(SORT_DISTANCE);
		req.setReturn(RETUREN_NEAR_DEVICE_ID);
		SearchResult search = tagSearchClient.search(req);
		List<Hit> hits = search.getHits().getHit();
		List<SimpleEntry<String, String>> result = new ArrayList<SimpleEntry<String, String>>();
		for (Hit h : hits) {
			try {
			SimpleEntry<String, String> entry = new SimpleEntry<String, String>(h.getFields().get(RESULT_FIELD_DEVICE_ID).get(0), h.getExprs().get(RESULT_FIELD_DISTANCE));
			result.add(entry);
			} catch (Exception e) {
				// silently
			}
		}
		return result;
	}

	@Override
	public List<TagSearchBean> retrieveNearDeviceInfosByTagSearch(Double latitude, Double longtitude, Long page) {
		String expression = String.format(DISTANCE_EXPRESSION, latitude, longtitude);
		SearchRequest req = new SearchRequest();
		req.setQuery(nearSearchPattern);
		req.setExpr(expression);
		req.setSort(SORT_DISTANCE);
		req.setQueryParser(QueryParser.Structured);
		req.setStart(10 * (page - 1));
		SearchResult search = tagSearchClient.search(req);
		List<Hit> hits = search.getHits().getHit();
		List<TagSearchBean> tags = new ArrayList<>();
		for (Hit h : hits) {
			try {
				TagSearchBean bean = filloutTagSearchBean(h);
				tags.add(bean);
			} catch (Exception e) {
				// silently
			}
		}
		return tags;
	}

	private TagCSSearchBean filloutTagSearchBean(Hit h) {
		Map<String, List<String>> fields = h.getFields();
		TagCSSearchBean bean = new TagCSSearchBean();
		String deviceId = fields.get(RESULT_FIELD_DEVICE_ID).get(0);
		bean.setDeviceId(deviceId);
		String latlon = fields.get(RESULT_FIELD_LOCATION).get(0);
		bean.setLocation(latlon);
		String deviceType = fields.get(RESULT_FIELD_DEVICE_TYPE).get(0);
		bean.setDeviceType(DeviceType.valueOf(deviceType));
		String snsToken = fields.get(RESULT_FIELD_SNS_TOKEN).get(0);
		bean.setSnsToken(snsToken);
		String point = fields.get(RESULT_FIELD_POINT).get(0);
		bean.setPoint(Long.parseLong(point));
		String alias = fields.get(RESULT_FIELD_ALIAS).get(0);
		bean.setAliasName(alias);
		String image = fields.get(RESULT_FIELD_IMAGE).get(0);
		bean.setImage(image);
		List<String> tags = fields.get(RESULT_FIELD_TAGS);
		String[] a = new String[tags.size()];
		tags.toArray(a);
		bean.setTags(a);
		return bean;
	}
	
	
	private TagSearchResultBean filloutTagSearchResultBean(Hit h) {
		Map<String, List<String>> fields = h.getFields();
		Map<String, String> exprs = h.getExprs();
		TagSearchResultBean bean = new TagSearchResultBean();
		String deviceId = fields.get(RESULT_FIELD_DEVICE_ID).get(0);
		bean.setDeviceId(deviceId);
		String latlon = fields.get(RESULT_FIELD_LOCATION).get(0);
		String[] split = latlon.split(",");
		bean.setLatitude(Double.parseDouble(split[0]));
		bean.setLongtitude(Double.parseDouble(split[1]));
		String distance = exprs.get(RESULT_FIELD_DISTANCE);
		bean.setDistance(Double.parseDouble(distance));
		String alias = fields.get(RESULT_FIELD_ALIAS).get(0);
		bean.setAliasName(alias);
		String image = fields.get(RESULT_FIELD_IMAGE).get(0);
		bean.setImage(image);
		List<String> tags = fields.get(RESULT_FIELD_TAGS);
		String[] a = new String[tags.size()];
		tags.toArray(a);
		bean.setTags(a);
		return bean;
	}
}
