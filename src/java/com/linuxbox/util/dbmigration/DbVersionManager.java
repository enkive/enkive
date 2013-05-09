package com.linuxbox.util.dbmigration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.util.Version;

/**
 * The purpose of this class is to maintain associations between software
 * version numbers and database version numbers. Some software versions may
 * require an updated version of the DB over the previous version of the
 * software. Other versions of the software might be fine with the previous
 * version. So rather than assume software version number equates to database
 * version number, this class breaks the strong association and allows them to
 * associate in different patterns.
 */
public class DbVersionManager {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.util.dbmigration.DbVersionManager");

	/**
	 * Turn this into a class, so there's never any confusion as to whether an
	 * integer refers to a software version or a db version.
	 */
	static public class DbVersion implements Comparable<DbVersion> {
		public final int ordinal;

		public DbVersion(int orindal) {
			this.ordinal = orindal;
		}

		@Override
		public int compareTo(DbVersion that) {
			return this.ordinal - that.ordinal;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof DbVersion) {
				return ordinal == ((DbVersion) other).ordinal;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return ordinal;
		}

		@Override
		public String toString() {
			return Integer.toString(ordinal);
		}

		public boolean matches(DbVersion that) {
			return this.ordinal == that.ordinal;
		}

		public boolean precedes(DbVersion that) {
			return this.ordinal < that.ordinal;
		}
	}

	static public class DbVersionManagerException extends Exception {
		private static final long serialVersionUID = 1L;

		public DbVersionManagerException(String message) {
			super(message);
		}

		public DbVersionManagerException(Throwable t) {
			super(t);
		}

		public DbVersionManagerException(String message, Throwable t) {
			super(message, t);
		}
	}

	protected Map<Version, DbVersion> softwareToDbVersion = new HashMap<Version, DbVersion>();
	protected Map<DbVersion, List<Version>> dbVersionToSoftwareVersions = new HashMap<DbVersion, List<Version>>();

	public void associate(Version v, DbVersion dbv)
			throws DbVersionManagerException {
		if (softwareToDbVersion.containsKey(v)) {
			throw new DbVersionManagerException(
					"already have association with software version key " + v);
		}
		softwareToDbVersion.put(v, dbv);

		if (dbVersionToSoftwareVersions.containsKey(dbv)) {
			List<Version> knownVersions = dbVersionToSoftwareVersions.get(dbv);
			knownVersions.add(v);
		} else {
			ArrayList<Version> versionList = new ArrayList<Version>();
			versionList.add(v);
			dbVersionToSoftwareVersions.put(dbv, versionList);
		}
		
		verifyMonotonicIncrease();
	}

	public DbVersion appropriateDbVersionFor(Version softwareVersion)
			throws DbVersionManagerException {
		if (!softwareToDbVersion.containsKey(softwareVersion)) {
			throw new DbVersionManagerException(
					"do not have DB version for software version "
							+ softwareVersion);
		}
		return softwareToDbVersion.get(softwareVersion);
	}

	public String softwareVersionsAppropriateToDbVersion(DbVersion dbv) {
		return softwareVersionsAppropriateToDbVersion(dbv, null);
	}

	public String softwareVersionsAppropriateToDbVersion(DbVersion dbv,
			String defaultValue) {
		List<Version> versions = dbVersionToSoftwareVersions.get(dbv);
		if (versions == null) {
			return defaultValue;
		} else if (1 == versions.size()) {
			return versions.get(0).versionString;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			boolean first = true;
			for (Version v : versions) {
				if (!first) {
					sb.append(", ");
				} else {
					first = false;
				}
				sb.append(v);
			}
			sb.append('}');
			return sb.toString();
		}
	}

	protected void verifyMonotonicIncrease() throws DbVersionManagerException {
		TreeSet<Version> softwareVersions = new TreeSet<Version>(
				softwareToDbVersion.keySet());
		Version previousSw = null;
		DbVersion previousDb = null;
		for (Version v : softwareVersions) {
			if (previousDb == null) {
				previousDb = softwareToDbVersion.get(v);
			} else {
				final DbVersion currentDb = softwareToDbVersion.get(v);
				if (currentDb.precedes(previousDb)) {
					final String s = previousSw.versionString + "->"
							+ previousDb.ordinal + " and " + v.versionString
							+ "->" + currentDb.ordinal;
					throw new DbVersionManagerException(
							"database versions not monotonically increasing: "
									+ s);
				}
				previousDb = currentDb;
			}
			previousSw = v;
		}
	}
}
