package com.magic.netty.serial.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magic.netty.serial.InputOutputFactory;

/**
 * @author when_how
 */
public class JsonSerialFactory implements InputOutputFactory {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final byte protocolVersion = 0;

	public static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public <T> T input(InputStream in, Class<T> clazz) throws IOException {
		return MAPPER.readValue(in, clazz);
	}

	@Override
	public <T> T input(byte[] data, Class<T> clazz) throws IOException {
		return input(data, 0, data.length, clazz);
	}

	@Override
	public <T> T input(byte[] data, int offset, Class<T> clazz) throws IOException {
		return input(data, offset, data.length - offset, clazz);
	}

	@Override
	public void output(OutputStream out, Object data) throws IOException {
		MAPPER.writeValue(out, data);
	}

	private <T> T input(byte[] data, int offset, int length, Class<T> clazz) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("json input with offset: " + new String(data, offset, length));
		}
		return MAPPER.readValue(data, offset, length, clazz);
	}

	@Override
	public byte getProtocolVersion() {
		return protocolVersion;
	}

}
