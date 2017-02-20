package com.tdsoft.bro.core.service.search.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.bean.TagESSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.bean.TagSearchResultBean;
import com.tdsoft.bro.core.exception.ArgumentInvalidException;
import com.tdsoft.bro.core.exception.BroException;
import com.tdsoft.bro.core.service.search.TagSearch;

@Service("tagSearchImpl")
public class TagElasticSearchImpl implements TagSearch {


	private static final Logger logger = LoggerFactory.getLogger(TagSearch.class);
	// private static final String RETUREN_NEAR_DEVICE_INFO = "device_id,distance,tags,location";
	// private static final String RETUREN_NEAR_DEVICE_ID = "device_id,distance,sns_token";

	private static final String RESULT_FIELD_DEVICE_ID = "I";
	private static final String RESULT_FIELD_TAGS = "TS";
	private static final String RESULT_FIELD_LOCATION = "LOC";
	public static final String RESULT_FIELD_LON = "lon";
	public static final String RESULT_FIELD_LAT = "lat";

	private static final String RESULT_FIELD_SNS_TOKEN = "S";
	private static final String RESULT_FIELD_DEVICE_TYPE = "T";
	private static final String RESULT_FIELD_POINT = "P";
	private static final String RESULT_FIELD_ALIAS = "A";
	private static final String RESULT_FIELD_IMAGE = "IMG";
	// private static final String RESULT_FIELD_DISTANCE = "distance";


	private static final String[] RESULT_FIELD_SEARCH_ALL = {RESULT_FIELD_DEVICE_ID, RESULT_FIELD_TAGS, RESULT_FIELD_LOCATION, RESULT_FIELD_ALIAS, RESULT_FIELD_IMAGE};

	private static final String[] RESULT_FIELD_SEARCH_DEVICE_ID = {RESULT_FIELD_DEVICE_ID, RESULT_FIELD_LOCATION};

	// =========================================================================


	private Client client;
	@Value("${core_search_hostname}")
	private String hostname = "localhost";
	@Value("${core_search_port}")
	private int port = 9300;
	private static final String INDEX = "bro";
	private static final String TYPE = "device";

	@PostConstruct
	private void init() throws UnknownHostException {
		client =
				TransportClient.builder().build()
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), port));
		boolean indexExists = client.admin().indices().prepareExists(INDEX).execute().actionGet().isExists();
		if (!indexExists) {
			client.admin().indices().prepareCreate(INDEX).execute().actionGet();
			throw new BroException("Not found index in elasticsearch! Please check!");
		}
	}

	@Override
	public void postTags(byte[] documents, List<String> allDoc) {
		BulkRequestBuilder bulkBuilder = client.prepareBulk();
		for (String tag : allDoc) {
			TagCacheBean tagSearchBean = JsonUtils.convertJsonToObject(tag, TagCacheBean.class);
			String bean = JsonUtils.convertObjectToJson(tagSearchBean.getField());
			IndexRequestBuilder idxRequest = client.prepareIndex(INDEX, TYPE, tagSearchBean.getField().getDeviceId()).setSource(bean);
			bulkBuilder.add(idxRequest);
		}
		BulkResponse response = bulkBuilder.execute().actionGet();
		if (response.hasFailures()) {
			logger.error("Post Tag Error Respone : {}" , response.buildFailureMessage());	
		}
	}

	private SearchRequestBuilder assembleSearchRequest(Double latitude, Double longtitude, Long page) {
		// distance criteria
		GeoDistanceQueryBuilder distanceQuery =
				QueryBuilders.geoDistanceQuery(RESULT_FIELD_LOCATION).lat(latitude).lon(longtitude).distance(3000, DistanceUnit.KILOMETERS);
		// path and page
		SearchRequestBuilder request =
				client.prepareSearch(INDEX).setTypes(TYPE).setPostFilter(distanceQuery).setFrom((int) (10 * (page - 1))).setSize(10);
		// field
		request.setFetchSource(RESULT_FIELD_SEARCH_ALL, null);
		// sort
		request.addSort(SortBuilders.geoDistanceSort(RESULT_FIELD_LOCATION).order(SortOrder.ASC).point(latitude, longtitude)
				.unit(DistanceUnit.KILOMETERS));
		return request;
	}


	@Override
	public List<TagSearchResultBean> retrieveNearTags(Double latitude, Double longtitude, Long page) {
		SearchRequestBuilder request = assembleSearchRequest(latitude, longtitude, page);
		// execute search request
		SearchResponse resp = request.execute().actionGet();
		List<TagSearchResultBean> result = new ArrayList<>();
		AtomicBoolean occuredError = new AtomicBoolean(false);
		for (SearchHit hit : resp.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			fillTagSearchResult(result, occuredError, hit, source);
		}
		if (occuredError.get()) {
			logger.error("Search error:{}", resp);
		}
		return result;
	}

	@Override
	public List<TagSearchResultBean> searchTags(Double latitude, Double longtitude, String keyword, Long page) {
		SearchRequestBuilder request = assembleSearchRequest(latitude, longtitude, page);
		// query
		request.setQuery(QueryBuilders.matchQuery(RESULT_FIELD_TAGS, keyword));
		// execute search request
		SearchResponse resp = request.execute().actionGet();
		List<TagSearchResultBean> result = new ArrayList<>();
		AtomicBoolean occuredError = new AtomicBoolean(false);
		for (SearchHit hit : resp.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			fillTagSearchResult(result, occuredError, hit, source);
		}
		if (occuredError.get()) {
			logger.error("Search error:{}", resp);
		}
		return result;
	}

	@Override
	public List<SimpleEntry<String, String>> retrieveNearDeviceId(Double latitude, Double longtitude) {
		// distance criteria
		GeoDistanceQueryBuilder distanceQuery =
				QueryBuilders.geoDistanceQuery(RESULT_FIELD_LOCATION).lat(latitude).lon(longtitude).distance(3000, DistanceUnit.KILOMETERS);
		// path and page
		SearchRequestBuilder request = client.prepareSearch(INDEX).setTypes(TYPE).setPostFilter(distanceQuery).setFrom(0).setSize(100);
		// field
		request.setFetchSource(RESULT_FIELD_SEARCH_DEVICE_ID, null);
		// sort
		request.addSort(SortBuilders.geoDistanceSort(RESULT_FIELD_LOCATION).order(SortOrder.ASC).point(latitude, longtitude)
				.unit(DistanceUnit.KILOMETERS));
		// execute search request
		SearchResponse resp = request.execute().actionGet();
		AtomicBoolean occuredError = new AtomicBoolean(false);
		List<SimpleEntry<String, String>> result = new ArrayList<SimpleEntry<String, String>>();
		for (SearchHit hit : resp.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			try {
				String deviceId = "";
				for (Entry<String, Object> value : source.entrySet()) {
					switch (value.getKey()) {
						case RESULT_FIELD_DEVICE_ID:
							deviceId = (String) value.getValue();
							break;
						default:
							throw new ArgumentInvalidException("Unknown field:" + value.getKey() + ", " + value.getValue());
					}
				}
				// distance
				Object[] sortValues = hit.getSortValues();
				SimpleEntry<String, String> entry = new SimpleEntry<String, String>(deviceId, (String) sortValues[0]);
				result.add(entry);
			} catch (Exception ex) {
				occuredError.set(true);
				logger.error("Found something wrong while parsing Search Result! Message :{}, Hit :{}, {}", ex.getMessage(),
						hit.getSourceAsString(), hit.getSortValues());
			}
		}
		if (occuredError.get()) {
			logger.error("Search error:{}", resp);
		}

		return result;
	}

	@Override
	public List<TagSearchBean> retrieveNearDeviceInfosByTagSearch(Double latitude, Double longtitude, Long page) {
		// TODO remove fetch source
		SearchRequestBuilder request = assembleSearchRequest(latitude, longtitude, page);
		// execute search request
		SearchResponse resp = request.execute().actionGet();
		List<TagSearchBean> result = new ArrayList<>();
		AtomicBoolean occuredError = new AtomicBoolean(false);
		for (SearchHit hit : resp.getHits().getHits()) {
			Map<String, Object> source = hit.getSource();
			filloutTagSearchBean(result, occuredError, hit, source);
		}
		if (occuredError.get()) {
			logger.error("Search error:{}", resp);
		}
		return result;
	}

	private void filloutTagSearchBean(List<TagSearchBean> result, AtomicBoolean occuredError, SearchHit hit, Map<String, Object> source) {
		TagESSearchBean bean = new TagESSearchBean();
		try {
			for (Entry<String, Object> value : source.entrySet()) {
				switch (value.getKey()) {
					case RESULT_FIELD_DEVICE_ID:
						bean.setDeviceId((String) value.getValue());
						break;
					case RESULT_FIELD_TAGS:
						@SuppressWarnings("unchecked")
						ArrayList<String> tags = (ArrayList<String>) value.getValue();
						bean.setTags(tags.toArray(new String[tags.size()]));
						break;
					case RESULT_FIELD_LOCATION:
						@SuppressWarnings("unchecked")
						HashMap<String, Double> loc = (HashMap<String, Double>) value.getValue();
						Double lon = loc.get(RESULT_FIELD_LON);
						Double lat = loc.get(RESULT_FIELD_LAT);
						bean.setLocation(loc);
						bean.setLatitude(lat);
						bean.setLongtitude(lon);
						break;
					case RESULT_FIELD_DEVICE_TYPE:
						DeviceType type = DeviceType.valueOf((String) value.getValue());
						bean.setDeviceType(type);
						break;
					case RESULT_FIELD_SNS_TOKEN:
						bean.setSnsToken((String) value.getValue());
						break;
					case RESULT_FIELD_POINT:
						bean.setPoint((long) value.getValue());
						break;
					case RESULT_FIELD_ALIAS:
						bean.setAliasName((String) value.getValue());
						break;
					case RESULT_FIELD_IMAGE:
						bean.setImage((String) value.getValue());
						break;
					default:
						throw new ArgumentInvalidException("Unknown field:" + value.getKey() + ", " + value.getValue());
				}
			}
			result.add(bean);
		} catch (Exception ex) {
			occuredError.set(true);
			logger.error("Found something wrong while parsing Search Result! Message :{}, Hit :{}, {}", ex.getMessage(),
					hit.getSourceAsString(), hit.getSortValues());
		}
	}

	private void fillTagSearchResult(List<TagSearchResultBean> result, AtomicBoolean occuredError, SearchHit hit, Map<String, Object> source) {
		TagSearchResultBean bean = new TagSearchResultBean();
		try {
			for (Entry<String, Object> value : source.entrySet()) {
				switch (value.getKey()) {
					case RESULT_FIELD_DEVICE_ID:
						bean.setDeviceId((String) value.getValue());
						break;
					case RESULT_FIELD_TAGS:
						@SuppressWarnings("unchecked")
						ArrayList<String> tags = (ArrayList<String>) value.getValue();
						bean.setTags(tags.toArray(new String[tags.size()]));
						break;
					case RESULT_FIELD_LOCATION:
						@SuppressWarnings("unchecked")
						HashMap<String, Double> loc = (HashMap<String, Double>) value.getValue();
						Double lon = loc.get(RESULT_FIELD_LON);
						Double lat = loc.get(RESULT_FIELD_LAT);
						bean.setLatitude(lat);
						bean.setLongtitude(lon);
						break;
					case RESULT_FIELD_ALIAS:
						bean.setAliasName((String) value.getValue());
						break;
					case RESULT_FIELD_IMAGE:
						bean.setImage((String) value.getValue());
						break;
					default:
						throw new ArgumentInvalidException("Unknown field:" + value.getKey() + ", " + value.getValue());
				}
			}
			// distance
			Object[] sortValues = hit.getSortValues();
			bean.setDistance((double) sortValues[0]);
			result.add(bean);
		} catch (Exception ex) {
			occuredError.set(true);
			logger.error("Found something wrong while parsing Search Result! Message :{}, Hit :{}, {}", ex.getMessage(),
					hit.getSourceAsString(), hit.getSortValues());
		}
	}
}
