package com.magic.netty.serial;

import java.io.IOException;
import java.io.InputStream;

public interface InputFactory {

	public <T> T input(byte[] data, Class<T> clazz) throws IOException;

	public <T> T input(byte[] data, int offest, Class<T> clazz) throws IOException;

	public <T> T input(InputStream in, Class<T> clazz) throws IOException;

}
