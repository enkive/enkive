package com.linuxbox.enkive.docstore.exceptions;

import java.io.IOException;

public class StorageException extends IOException {
	private static final long serialVersionUID = -8935195197908675515L;

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable t) {
		super(message, t);
	}

	public StorageException(Throwable t) {
		super(t);
	}
}
