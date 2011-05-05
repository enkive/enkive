package com.linuxbox.enkive.docstore.mongogrid;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class ControlReleaseException extends DocStoreException {
	private static final long serialVersionUID = 5646914906405589115L;

	public ControlReleaseException(String identifier) {
		super("file: \"" + identifier + "\"");
	}
}
