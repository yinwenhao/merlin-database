package com.magic.bitcask.exception;

import com.magic.exception.BaseException;

public class ReadOnlyException extends BaseException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadOnlyException() {
	}

	public ReadOnlyException(String reason) {
		super(reason);
	}

}
