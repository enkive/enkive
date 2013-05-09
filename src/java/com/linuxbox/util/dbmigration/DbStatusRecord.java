package com.linuxbox.util.dbmigration;

import java.util.Date;

import com.linuxbox.util.dbmigration.DbVersionManager.DbVersion;

public class DbStatusRecord {
	public static enum Status {
		STORED(0), MIGRATING(1), ERROR(-1);

		public final int code;

		private Status(int code) {
			this.code = code;
		}
	}

	public final DbVersion dbVersion;
	public final Status status;
	public final Date timestamp;

	public DbStatusRecord(DbVersion version, Status status, Date timestamp) {
		this.dbVersion = version;
		this.status = status;
		this.timestamp = timestamp;
	}
}
