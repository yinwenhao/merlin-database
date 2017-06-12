package com.magic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class Options {

	public static void usePropertiesFromConfFile(Object o, File conf)
			throws IOException, IllegalArgumentException, IllegalAccessException {
		String packageName = o.getClass().getPackage().getName();
		Properties pro = new Properties();
		FileInputStream in = new FileInputStream(conf);
		pro.load(in);
		in.close();
		for (Field f : o.getClass().getFields()) {
			String v = pro.getProperty(packageName + "." + f.getName());
			if (v == null) {
				v = pro.getProperty(f.getName());
			}
			if (v != null) {
				// 使用配置文件里的值
				setField(o, f, v);
			}
		}
	}

	public static void useSystemProperties(Object o) throws IllegalArgumentException, IllegalAccessException {
		String packageName = o.getClass().getPackage().getName();
		for (Field f : o.getClass().getFields()) {
			String v = System.getProperty(packageName + "." + f.getName());
			if (v != null) {
				// 使用-D参数设置的值
				setField(o, f, v);
			}
		}
	}

	private static void setField(Object o, Field f, String v) throws IllegalArgumentException, IllegalAccessException {
		if (f.getType().equals(String.class)) {
			f.set(o, v);
		} else if (f.getType().equals(int.class) || f.getType().equals(Integer.class)) {
			f.setInt(o, Integer.valueOf(v));
		} else if (f.getType().equals(short.class) || f.getType().equals(Short.class)) {
			f.setShort(o, Short.valueOf(v));
		} else if (f.getType().equals(long.class) || f.getType().equals(Long.class)) {
			f.setLong(o, Long.valueOf(v));
		} else if (f.getType().equals(double.class) || f.getType().equals(Double.class)) {
			f.setDouble(o, Double.valueOf(v));
		} else if (f.getType().equals(float.class) || f.getType().equals(Float.class)) {
			f.setFloat(o, Float.valueOf(v));
		} else if (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class)) {
			f.setBoolean(o, Boolean.valueOf(v));
		}
	}

}
