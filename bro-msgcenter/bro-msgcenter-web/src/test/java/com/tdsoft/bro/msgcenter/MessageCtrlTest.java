package com.tdsoft.bro.msgcenter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-config.xml")
public class MessageCtrlTest {
	@Rule
	public RestDocumentation restDocumentation = new RestDocumentation("target/generated-snippets");

	@Autowired
	private WebApplicationContext context;
	@Resource(name = "deviceRedisClient")
	private RedisTemplate<String, String> redis;

	private MockMvc mockMvc;
	private static final String SESSION_ID = "111111111111111111111111111111111111";
	private static final String DEVICE_ID = "test@B";
	private static final String POINT = "137930726724";

	@Value("${ctrl_device_received_messages}")
	private String retrieveMessagesPath;

	@Value("${ctrl_device_sent_messages}")
	private String checkMessageSuccessfulPath;

	@Value("${ctrl_broadcast_messages}")
	private String getAllBroadcastMessagesPath;

	@Before
	public void setUp() throws Exception {
		HashMap<String, String> data = new HashMap<String, String>();
		addSessionData(data);
		addDeviceInfo(data);
		Set<TypedTuple<String>> set = new HashSet<ZSetOperations.TypedTuple<String>>();
		addRecevicedMessage(set);
		addSentMessage(set);
		addBroMessage(set);
		DelegatingFilterProxy sessionFilter = new DelegatingFilterProxy("springSessionRepositoryFilter", context);
		this.mockMvc =
				MockMvcBuilders.webAppContextSetup(this.context).addFilters(sessionFilter)
						.apply(documentationConfiguration(this.restDocumentation)).build();
	}

	private void addDeviceInfo(HashMap<String, String> data) {
		data.clear();
		data.put("U", "test");
		data.put("LON", "1");
		data.put("LAT", "1");
		data.put("S", "abcde12345");
		data.put("T", "B");
		data.put("N", "0");
		redis.opsForHash().putAll(DEVICE_ID + ":D", data);
	}

	private void addSessionData(HashMap<String, String> data) {
		data.clear();
		data.put("I", DEVICE_ID);
		redis.opsForHash().putAll("S:sessions:" + SESSION_ID, data);
	}

	private void addRecevicedMessage(Set<TypedTuple<String>> set) {
		set.clear();
		ZSetOperations.TypedTuple<String> a =
				new DefaultTypedTuple<>(
						"{\"C\":\"we dfgh\",\"I\":\"12345@B\",\"R\":\"test@B\",\"CI\":\"1450685469941\",\"CR\":1450685471819}",
						1450685471819d);
		set.add(a);
		a =
				new DefaultTypedTuple<>(
						"{\"C\":\"gfdghrhhhg\",\"I\":\"12345@B\",\"R\":\"test@B\",\"CI\":\"1450685491706\",\"CR\":1450685492497}",
						1450685492497d);
		set.add(a);
		redis.opsForZSet().add(DEVICE_ID + ":M:R", set);
	}

	private void addSentMessage(Set<TypedTuple<String>> set) {
		set.clear();
		ZSetOperations.TypedTuple<String> a =
				new DefaultTypedTuple<>(
						"{\"C\":\"we dfgh\",\"I\":\"test@B\",\"R\":\"12345@B\",\"CI\":\"1450685469941\",\"CR\":1450685471819}",
						1450685471819d);
		set.add(a);
		a =
				new DefaultTypedTuple<>(
						"{\"C\":\"gfdghrhhhg\",\"I\":\"test@B\",\"R\":\"12345@B\",\"CI\":\"1450685491706\",\"CR\":1450685492497}",
						1450685492497d);
		set.add(a);
		redis.opsForZSet().add(DEVICE_ID + ":M:S", set);
	}

	private void addBroMessage(Set<TypedTuple<String>> set) {
		set.clear();
		ZSetOperations.TypedTuple<String> a = new DefaultTypedTuple<>("{\"C\":\"we dfgh\",\"CR\":1450685471819}", 1450685471819d);
		set.add(a);
		redis.opsForZSet().add("P:" + POINT + ":M", set);
	}


	@Test
	public void retrieveMessages() throws Exception {
		this.mockMvc
				.perform(get(retrieveMessagesPath, DEVICE_ID, 0, 1).cookie(new Cookie("S", SESSION_ID)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document(
						"接收聊天訊息",
						pathParameters(parameterWithName("deviceId").description("裝置代號"),
								parameterWithName("lastUpdateTime").description("上次接收時間，以epoch格式，GMT+8時區"), parameterWithName("page")
										.description("頁數")),
						responseFields(fieldWithPath("[].I").description("發送者代號"), fieldWithPath("[].I").description("發送者代號"),
								fieldWithPath("[].C").description("訊息內容"), fieldWithPath("[].R").description("接收者代號"),
								fieldWithPath("[].CR").description("訊息發送時間"), fieldWithPath("[].CI").description("客戶端發送代號"))));
	}

	@Test
	public void checkMessageSuccessful() throws Exception {
		this.mockMvc
				.perform(
						get(checkMessageSuccessfulPath, DEVICE_ID, 1450685491706l).cookie(new Cookie("S", SESSION_ID)).accept(
								MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document(
						"驗證訊息是否已處理",
						pathParameters(
								parameterWithName("deviceId").description("裝置代號"),
								parameterWithName("clientMessageId")
										.description(
												"發送者端訊息代號(發送者當下的currentTimeMillis)，epoch格式，GMT+8時區，發送端可利用此代號查詢訊息是否處理成功，合理狀況下不可能同毫秒下有兩封訊息從裝置送出 	e.g:1441901795512")),
						responseFields(fieldWithPath("successful").description("訊息是否處理完成"))));
	}

	@Test
	public void getAllBroadcastMessages() throws Exception {
		this.mockMvc
				.perform(
						get(getAllBroadcastMessagesPath, 12, 123, 0, 1).cookie(new Cookie("S", SESSION_ID)).accept(
								MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document(
						"接收廣播訊息",
						pathParameters(parameterWithName("latitude").description("緯度"), parameterWithName("longtitude").description("經度"),
								parameterWithName("lastUpdateTime").description("上次接收時間，以epoch格式，GMT+8時區"), parameterWithName("page")
										.description("頁數")),
						responseFields(fieldWithPath("[].I").description("發送者代號"), fieldWithPath("[].I").description("發送者代號"),
								fieldWithPath("[].C").description("廣播內容"), fieldWithPath("[].R").description("接收者代號"),
								fieldWithPath("[].CR").description("訊息發送時間"), fieldWithPath("[].T").description("標題"),
								fieldWithPath("[].R").description("距離"), fieldWithPath("[].LON").description("經度"), fieldWithPath("[].LAT")
										.description("緯度"), fieldWithPath("[].N").description("發送者裝置序號"), fieldWithPath("[].MT")
										.description("訊息類別"))));
	}

	@Test
	public void unAuth401() throws Exception {
		this.mockMvc.perform(get("/messages/device/test@B/0/1").cookie(new Cookie("SS", SESSION_ID)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized()).andDo(document("receviceMessage"));
	}

	@After
	public void tearDown() {
		redis.delete("S:sessions:" + SESSION_ID);
		redis.keys(DEVICE_ID + "*").forEach(e -> {
			redis.delete(e);
		});;
		redis.delete("P:" + POINT + ":M");
	}
}
