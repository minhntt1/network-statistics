package com.home.network.statistic.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtil {
	private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).build();

	private JsonUtil() {
	}

	@SneakyThrows
	public static String toJson(Object o) {
		return mapper.writeValueAsString(o);
	}

	@SneakyThrows
	public static <T> T fromJson(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			log.error("json parse exception: {} for {}", json, clazz.getCanonicalName(), e);
			throw e;
		}
	}

	@SneakyThrows
	public static <T> List<T> fromJsonToArray(String json, Class<T> clazz) {
		try {
			return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (Exception e) {
			log.error("json parse exception: {} for {}", json, clazz.getCanonicalName(), e);
			throw e;
		}
	}

}
