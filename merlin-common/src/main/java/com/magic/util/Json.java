package com.magic.util;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static <T> T readValue(File src, Class<T> valueType)
			throws IOException, JsonParseException, JsonMappingException {
		return mapper.readValue(src, valueType);
	}

}
