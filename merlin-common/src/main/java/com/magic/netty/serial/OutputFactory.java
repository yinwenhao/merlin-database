package com.magic.netty.serial;

import java.io.IOException;
import java.io.OutputStream;

public interface OutputFactory {

	public void output(OutputStream out, Object data) throws IOException;

	public byte getProtocolVersion();

}
