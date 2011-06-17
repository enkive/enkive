package com.linuxbox.enkive.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class ArchiverUtils {

	public static Collection<Object[]> getAllTestFiles(File dir) {
		Collection<Object[]> files = new ArrayList<Object[]>();
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					files.add(new File[] { file });
				} else {
					return getAllTestFiles(file);
				}
			}
		}
		return files;
	}
	
}
