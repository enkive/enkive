package com.linuxbox.util;


/**
 * This is a Version class that doesn't track seen versions.  It's used for
 * short-lived version objects that need to be compared to full version objects,
 * but not kept around.
 * @author dang
 *
 */
public class EphemeralVersion extends Version {

	public EphemeralVersion(int major, int minor, Type type, int ordinal)
			throws VersionException {
		super(major, minor, type, ordinal);
	}

	public EphemeralVersion(String versionString, int ordinal)
			throws VersionException {
		super(versionString, ordinal);
	}

	public EphemeralVersion(int major, int minor, Type type, int ordinal,
			String versionString) {
		super(major, minor, type, ordinal, versionString);
	}

	protected void checkOrdinal() throws VersionException {
		// Dummy checkOrdinal to *not* save versions or check monotonic
	}
}
