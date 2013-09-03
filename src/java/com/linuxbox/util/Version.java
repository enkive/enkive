package com.linuxbox.util;

import java.util.HashMap;
import java.util.Map;

public class Version implements Comparable<Version> {
	public static class VersionException extends RuntimeException {
		private static final long serialVersionUID = -8848841261513645899L;

		protected VersionException(String message) {
			super(message);
		}
	}

	public static enum Type {
		UNUSED("UNUSED", "UNUSED"), ALPHA("alpha", "a"), BETA("beta", "b"), RELEASE_CANDIDATE(
				"release candidate", "rc"), PRODUCTION("production", "");

		final String name;
		final String abbreviation;

		Type(String name, String abbreviation) {
			this.name = name;
			this.abbreviation = abbreviation;
		}
	};

	public final int major;
	public final int minor;
	public final Type type;

	public final String versionString;

	public final int versionOrdinal;

	static int lastOrdinalSeen = Integer.MIN_VALUE;

	static protected Map<Integer, String> ordinalToVersionStringMap = new HashMap<Integer, String>();

	public Version(int major, int minor, Type type, int ordinal)
			throws VersionException {
		this(major, minor, type, ordinal, major + "." + minor
				+ type.abbreviation);
	}

	public Version(String versionString, int ordinal) throws VersionException {
		this(-1, -1, Type.UNUSED, ordinal, versionString);
	}

	public Version(int major, int minor, Type type, int ordinal,
			String versionString) {
		this.major = major;
		this.minor = minor;
		this.type = type;
		this.versionOrdinal = ordinal;
		this.versionString = versionString;
		checkOrdinal();
	}

	protected void checkOrdinal() throws VersionException {
		if (versionOrdinal <= lastOrdinalSeen) {
			throw new VersionException(
					"version ordinals not monotonically increasing");
		}

		lastOrdinalSeen = versionOrdinal;
		ordinalToVersionStringMap.put(versionOrdinal, versionString);
	}

	public String toString() {
		return versionString;
	}

	@Override
	public int compareTo(Version that) {
		return this.versionOrdinal - that.versionOrdinal;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Version) {
			return versionOrdinal == ((Version) other).versionOrdinal;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return versionOrdinal;
	}

	public static String versionStringFromOrdinal(int ordinal) {
		return ordinalToVersionStringMap.get(ordinal);
	}
}
