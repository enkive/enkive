package com.linuxbox.util.dbmigration;

import java.util.Date;

public class DBStatusRecord {
	public static enum Status {
		STORED(0), MIGRATING(1), ERROR(-1);

		public final int code;

		private Status(int code) {
			this.code = code;
		}
	}

	public final int version;
	public final Status status;
	public final Date timestamp;

	public DBStatusRecord(int version, Status status, Date timestamp) {
		this.version = version;
		this.status = status;
		this.timestamp = timestamp;
	}
}
