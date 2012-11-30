package com.linuxbox.util.dbmigration;

public class DBMigrationException extends Exception {
	private static final long serialVersionUID = -5592335264801790268L;

	public DBMigrationException(String message) {
		super(message);
	}

	public DBMigrationException(Throwable cause) {
		super(cause);
	}

	public DBMigrationException(String message, Throwable cause) {
		super(message, cause);
	}
}
