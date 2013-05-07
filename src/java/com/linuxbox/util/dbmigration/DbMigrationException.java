package com.linuxbox.util.dbmigration;

public class DbMigrationException extends Exception {
	private static final long serialVersionUID = -5592335264801790268L;

	public DbMigrationException(String message) {
		super(message);
	}

	public DbMigrationException(Throwable cause) {
		super(cause);
	}

	public DbMigrationException(String message, Throwable cause) {
		super(message, cause);
	}
}
