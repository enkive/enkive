package com.linuxbox.util.dbmigration;

import java.util.Date;

import com.linuxbox.util.dbmigration.DbVersionManager.DbVersion;

public class DbStatusRecord {
	public static enum Status {
		/**
		 * we want the codes to be in chronological sequence, esp. when trying
		 * to retrieve the latest version
		 */
		MIGRATING(0), STORED(1), ERROR(2);

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
