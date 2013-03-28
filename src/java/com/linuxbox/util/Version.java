package com.linuxbox.util;

public class Version {
	public static enum Type {
		ALPHA("alpha", "a"), BETA("beta", "b"), RELEASE_CANDIDATE(
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

	/**
	 * An ordinal value to make comparing versions easier. It's a set of four
	 * bit fields listed here from least to most significant:
	 * 
	 * # 12 bits reserved
	 * 
	 * # 3 bits for VersionType
	 * 
	 * # 10 bits for minor number
	 * 
	 * # 6 bits for major number
	 * 
	 */
	public final int versionOrdinal;

	public Version(int major, int minor, Type type) {
		this.major = major;
		this.minor = minor;
		this.type = type;
		this.versionString = major + "." + minor + type.abbreviation;
		this.versionOrdinal = major << 25 | minor << 15 | type.ordinal() << 12;
	}

	public String toString() {
		return versionString;
	}
}
