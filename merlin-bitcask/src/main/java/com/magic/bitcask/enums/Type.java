package com.magic.bitcask.enums;

public enum Type {

	WRITE, MERGE;

	public String getTypeName() {
		if (this == MERGE)
			return "merge";
		if (this == WRITE)
			return "write";
		throw new RuntimeException();
	}

}
