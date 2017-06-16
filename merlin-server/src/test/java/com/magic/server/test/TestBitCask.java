package com.magic.server.test;

import java.io.File;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.impl.BitCaskFactoryImpl;
import com.magic.server.options.ServerOptions;

public class TestBitCask {

	public static void main(String[] args) throws Exception {
		BitCaskFactory bitCaskFactory = new BitCaskFactoryImpl();
		ServerOptions opts = new ServerOptions();
		BitCask bitcask = bitCaskFactory.createBitCask(new File("../bitcask/"),
				opts.bitCaskOptions);
		// bitcask.put("aaa", "12345");
		System.out.println(bitcask.get("aaa"));
		bitcask.set("aaa", "123456", 1234567890);
		bitcask.setWithExpire("bbb", "2r3546457", 1234567890, 1000);
		System.out.println(bitcask.get("aaa"));
	}

}
