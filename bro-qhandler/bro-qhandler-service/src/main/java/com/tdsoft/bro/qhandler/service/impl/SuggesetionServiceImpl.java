package com.tdsoft.bro.qhandler.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.common.util.KeyValuePair;
import com.tdsoft.bro.qhandler.service.ISuggestionService;
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:test-config.xml"})

@Service("suggesetionServiceImpl")
public class SuggesetionServiceImpl implements ISuggestionService {
	private static final Logger LOG = LoggerFactory.getLogger(SuggesetionServiceImpl.class);
	/**
	 * Key: country name , e.g: Taiwan Value: google region code , e.g: p12
	 */
	LinkedHashMap<String, String> googleRegionCode = new LinkedHashMap<String, String>();
	@Resource(name = "defaultRedisClient")
	RedisTemplate<String, String> redisClient;
	@Value("${manager_suggestion_key}")
	String suggestionKey = "SG";

	@PostConstruct
	public void init() {
		// 1. $("#geo-picker-menu") 取出所有div
		// 2. 將div資料丟到fiddle上 並使用以下語法，因為google頁面會擋住array顯示
		// var result = [];
		// $("[class='goog-menuitem ']", tmpl).each(function( index ) {
		// result.push({'key':$(this).text(),'value': $(this).attr('data-id')});
		// 3. 顯示結果如下
		// [{"name":"Taiwan","value":"p12"},{"name":"Argentina","value":"p30"},{"name":"Australia","value":"p8"},{"name":"Austria","value":"p44"},{"name":"Belgium","value":"p41"},{"name":"Brazil","value":"p18"},{"name":"Canada","value":"p13"},{"name":"Chile","value":"p38"},{"name":"Colombia","value":"p32"},{"name":"Czech
		// Republic","value":"p43"},{"name":"Denmark","value":"p49"},{"name":"Egypt","value":"p29"},{"name":"Finland","value":"p50"},{"name":"France","value":"p16"},{"name":"Germany","value":"p15"},{"name":"Greece","value":"p48"},{"name":"Hong
		// Kong","value":"p10"},{"name":"Hungary","value":"p45"},{"name":"India","value":"p3"},{"name":"Indonesia","value":"p19"},{"name":"Israel","value":"p6"},{"name":"Italy","value":"p27"},{"name":"Japan","value":"p4"},{"name":"Kenya","value":"p37"},{"name":"Malaysia","value":"p34"},{"name":"Mexico","value":"p21"},{"name":"Netherlands","value":"p17"},{"name":"Nigeria","value":"p52"},{"name":"Norway","value":"p51"},{"name":"Philippines","value":"p25"},{"name":"Poland","value":"p31"},{"name":"Portugal","value":"p47"},{"name":"Romania","value":"p39"},{"name":"Russia","value":"p14"},{"name":"Saudi
		// Arabia","value":"p36"},{"name":"Singapore","value":"p5"},{"name":"South
		// Africa","value":"p40"},{"name":"South
		// Korea","value":"p23"},{"name":"Spain","value":"p26"},{"name":"Sweden","value":"p42"},{"name":"Switzerland","value":"p46"},{"name":"Thailand","value":"p33"},{"name":"Turkey","value":"p24"},{"name":"Ukraine","value":"p35"},{"name":"United
		// Kingdom","value":"p9"},{"name":"United
		// States","value":"p1"},{"name":"Vietnam","value":"p28"}]
		// [{\"name\":\"Taiwan\",\"value\":\"p12\"},{\"name\":\"Argentina\",\"value\":\"p30\"},{\"name\":\"Australia\",\"value\":\"p8\"},{\"name\":\"Austria\",\"value\":\"p44\"},{\"name\":\"Belgium\",\"value\":\"p41\"},{\"name\":\"Brazil\",\"value\":\"p18\"},{\"name\":\"Canada\",\"value\":\"p13\"},{\"name\":\"Chile\",\"value\":\"p38\"},{\"name\":\"Colombia\",\"value\":\"p32\"},{\"name\":\"Czech
		// Republic\",\"value\":\"p43\"},{\"name\":\"Denmark\",\"value\":\"p49\"},{\"name\":\"Egypt\",\"value\":\"p29\"},{\"name\":\"Finland\",\"value\":\"p50\"},{\"name\":\"France\",\"value\":\"p16\"},{\"name\":\"Germany\",\"value\":\"p15\"},{\"name\":\"Greece\",\"value\":\"p48\"},{\"name\":\"Hong
		// Kong\",\"value\":\"p10\"},{\"name\":\"Hungary\",\"value\":\"p45\"},{\"name\":\"India\",\"value\":\"p3\"},{\"name\":\"Indonesia\",\"value\":\"p19\"},{\"name\":\"Israel\",\"value\":\"p6\"},{\"name\":\"Italy\",\"value\":\"p27\"},{\"name\":\"Japan\",\"value\":\"p4\"},{\"name\":\"Kenya\",\"value\":\"p37\"},{\"name\":\"Malaysia\",\"value\":\"p34\"},{\"name\":\"Mexico\",\"value\":\"p21\"},{\"name\":\"Netherlands\",\"value\":\"p17\"},{\"name\":\"Nigeria\",\"value\":\"p52\"},{\"name\":\"Norway\",\"value\":\"p51\"},{\"name\":\"Philippines\",\"value\":\"p25\"},{\"name\":\"Poland\",\"value\":\"p31\"},{\"name\":\"Portugal\",\"value\":\"p47\"},{\"name\":\"Romania\",\"value\":\"p39\"},{\"name\":\"Russia\",\"value\":\"p14\"},{\"name\":\"Saudi
		// Arabia\",\"value\":\"p36\"},{\"name\":\"Singapore\",\"value\":\"p5\"},{\"name\":\"South
		// Africa\",\"value\":\"p40\"},{\"name\":\"South
		// Korea\",\"value\":\"p23\"},{\"name\":\"Spain\",\"value\":\"p26\"},{\"name\":\"Sweden\",\"value\":\"p42\"},{\"name\":\"Switzerland\",\"value\":\"p46\"},{\"name\":\"Thailand\",\"value\":\"p33\"},{\"name\":\"Turkey\",\"value\":\"p24\"},{\"name\":\"Ukraine\",\"value\":\"p35\"},{\"name\":\"United
		// Kingdom\",\"value\":\"p9\"},{\"name\":\"United
		// States\",\"value\":\"p1\"},{\"name\":\"Vietnam\",\"value\":\"p28\"}]
		String data =
				"[{\"key\":\"Taiwan\",\"value\":\"p12\"},{\"key\":\"Argentina\",\"value\":\"p30\"},{\"key\":\"Australia\",\"value\":\"p8\"},{\"key\":\"Austria\",\"value\":\"p44\"},{\"key\":\"Belgium\",\"value\":\"p41\"},{\"key\":\"Brazil\",\"value\":\"p18\"},{\"key\":\"Canada\",\"value\":\"p13\"},{\"key\":\"Chile\",\"value\":\"p38\"},{\"key\":\"Colombia\",\"value\":\"p32\"},{\"key\":\"Czech Republic\",\"value\":\"p43\"},{\"key\":\"Denmark\",\"value\":\"p49\"},{\"key\":\"Egypt\",\"value\":\"p29\"},{\"key\":\"Finland\",\"value\":\"p50\"},{\"key\":\"France\",\"value\":\"p16\"},{\"key\":\"Germany\",\"value\":\"p15\"},{\"key\":\"Greece\",\"value\":\"p48\"},{\"key\":\"Hong Kong\",\"value\":\"p10\"},{\"key\":\"Hungary\",\"value\":\"p45\"},{\"key\":\"India\",\"value\":\"p3\"},{\"key\":\"Indonesia\",\"value\":\"p19\"},{\"key\":\"Israel\",\"value\":\"p6\"},{\"key\":\"Italy\",\"value\":\"p27\"},{\"key\":\"Japan\",\"value\":\"p4\"},{\"key\":\"Kenya\",\"value\":\"p37\"},{\"key\":\"Malaysia\",\"value\":\"p34\"},{\"key\":\"Mexico\",\"value\":\"p21\"},{\"key\":\"Netherlands\",\"value\":\"p17\"},{\"key\":\"Nigeria\",\"value\":\"p52\"},{\"key\":\"Norway\",\"value\":\"p51\"},{\"key\":\"Philippines\",\"value\":\"p25\"},{\"key\":\"Poland\",\"value\":\"p31\"},{\"key\":\"Portugal\",\"value\":\"p47\"},{\"key\":\"Romania\",\"value\":\"p39\"},{\"key\":\"Russia\",\"value\":\"p14\"},{\"key\":\"Saudi Arabia\",\"value\":\"p36\"},{\"key\":\"Singapore\",\"value\":\"p5\"},{\"key\":\"South Africa\",\"value\":\"p40\"},{\"key\":\"South Korea\",\"value\":\"p23\"},{\"key\":\"Spain\",\"value\":\"p26\"},{\"key\":\"Sweden\",\"value\":\"p42\"},{\"key\":\"Switzerland\",\"value\":\"p46\"},{\"key\":\"Thailand\",\"value\":\"p33\"},{\"key\":\"Turkey\",\"value\":\"p24\"},{\"key\":\"Ukraine\",\"value\":\"p35\"},{\"key\":\"United Kingdom\",\"value\":\"p9\"},{\"key\":\"United States\",\"value\":\"p1\"},{\"key\":\"Vietnam\",\"value\":\"p28\"}]";

		@SuppressWarnings("rawtypes")
		List<KeyValuePair> list = JsonUtils.convertJsonToList(data, KeyValuePair.class);
		list.sort((l, r) -> {
			int sl = Integer.parseInt(l.getValue().toString().substring(1));
			int sr = Integer.parseInt(r.getValue().toString().substring(1));
			return Integer.compare(sl, sr);
		});
		Locale[] availableLocales = Locale.getAvailableLocales();
		list.forEach(e -> {
			for (Locale locale : availableLocales) {
				if (locale.getDisplayCountry(Locale.US).equals(e.getKey().toString())) {
					googleRegionCode.put(locale.toString(), e.getValue().toString());
					break;
				}
			}
		});
	}

	@Override
	public void reloadSuggestion() {
		// $('.tab .names a')[1].text http://lib.colostate.edu/wildlife/atoz.php?letter=ALL
		List<ListenableFuture<ResponseEntity<String>>> tasks = new LinkedList<>();
		String googleTransBaseUrl = "https://www.google.com/trends/hottrends/atom/feed?pn={}";
		AsyncRestTemplate rest = new AsyncRestTemplate();
		for (Entry<String, String> entrySet : googleRegionCode.entrySet()) {
			ListenableFuture<ResponseEntity<String>> forEntity =
					rest.getForEntity(googleTransBaseUrl.replace("{}", entrySet.getValue()), String.class);
			forEntity.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

				@Override
				public void onSuccess(ResponseEntity<String> result) {
					Document xmlDoc = Jsoup.parse(result.getBody());
					Elements as = xmlDoc.select("item title");
					List<String> suggestions = new LinkedList<>();
					for (Element a : as) {
						String suggestion = a.text();
						suggestions.add(suggestion);
					}
					String locale = entrySet.getKey();
					redisClient.opsForHash().put(suggestionKey, locale.substring(0, 5), JsonUtils.convertObjectToJson(suggestions));
				}

				@Override
				public void onFailure(Throwable ex) {
					ex.printStackTrace();
				}
			});
			tasks.add(forEntity);
			// Future<Response> f = asyncHttpClient.prepareGet(googleTransBaseUrl.replace("{}",
			// entrySet.getValue()))
			// .execute(new AsyncCompletionHandler<Response>() {
			//
			// @Override
			// public Response onCompleted(Response response) throws Exception {
			// // URL("http://lib.colostate.edu/wildlife/atoz.php?letter=ALL");
			// Document xmlDoc = Jsoup.parse(response.getResponseBody());
			// Elements as = xmlDoc.select("item title");
			// List<String> suggestions = new LinkedList<>();
			// for (Element a : as) {
			// String suggestion = a.text();
			// suggestions.add(suggestion);
			// }
			// String locale = entrySet.getKey();
			// redisClient.opsForHash().put(suggestionKey, locale.substring(0, 5),
			// JsonUtils.convertObjectToJson(suggestions));
			// return response;
			// }
			//
			// @Override
			// public void onThrowable(Throwable t) {
			// t.printStackTrace();
			// }
			// });
			// tasks.add(f);
		}
		tasks.forEach(e -> {
			try {
				e.get();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		redisClient.convertAndSend(suggestionKey, "");
		LOG.info("Finish reloading suggestion, {}", LocalDateTime.now());
	}
}
