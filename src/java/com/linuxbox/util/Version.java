package com.linuxbox.util;

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

	public Version(int major, int minor, Type type, int ordinal)
			throws VersionException {
		this.major = major;
		this.minor = minor;
		this.type = type;
		this.versionString = major + "." + minor + type.abbreviation;
		this.versionOrdinal = ordinal;
		checkOrdinal();
	}

	public Version(String versionString, int ordinal) throws VersionException {
		this.major = -1;
		this.minor = -1;
		this.type = Type.UNUSED;
		this.versionString = versionString;
		this.versionOrdinal = ordinal;
		checkOrdinal();
	}

	protected void checkOrdinal() throws VersionException {
		if (versionOrdinal <= lastOrdinalSeen) {
			throw new VersionException(
					"version ordinals not monotonically increasing");
		} else {
			lastOrdinalSeen = versionOrdinal;
		}
	}

	public String toString() {
		return versionString;
	}

	@Override
	public int compareTo(Version other) {
		return versionOrdinal - other.versionOrdinal;
	}
}
