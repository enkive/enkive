package com.linuxbox.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DirectoryManagement {
	public static void verifyDirectory(File dir, String description)
			throws IOException {
		String path = dir.getCanonicalPath();

		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new FileNotFoundException("could not find or create "
						+ description + " \"" + path + "\"");
			}
		} else if (!dir.isDirectory()) {
			throw new FileNotFoundException(description + " \"" + path
					+ "\" is not a directory");
		}

		// either directory existed or it was just created; now test it

		if (!dir.canWrite()) {
			throw new IOException("cannot write to " + description + " \""
					+ path + "\"");
		}

		if (!dir.canExecute()) {
			throw new IOException("cannot access " + description + " \"" + path
					+ "\"");
		}

		if (!dir.canRead()) {
			throw new IOException("cannot read from " + description + " \""
					+ path + "\"");
		}
	}

	public static void verifyDirectory(String path, String description)
			throws IOException {
		File dir = new File(path);
		verifyDirectory(dir, description);
	}
}
