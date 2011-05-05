package com.linuxbox.enkive.docstore.mongogrid;

import java.util.Date;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class ControlAcquisitionException extends DocStoreException {
	private static final long serialVersionUID = -7252939844183791029L;

	public ControlAcquisitionException(String identifier) {
		super(identifier, null);
	}

	public ControlAcquisitionException(String identifier, Date date) {
		super("file: \"" + identifier + "\"; date control acquired: "
				+ (date == null ? "unknown" : date.toString()));
	}
}
