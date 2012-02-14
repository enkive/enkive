/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.importer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;

public class ThreadedMessageImporter {
	static final int DEFAULT_THREAD_POOL_SIZE = 2;
	static final int CONVERSION_PORT = 2526;

	InetAddress host;
	int port;
	ExecutorService threadPool;
	Set<String> directories;

	ThreadedMessageImporter(String host, int port, int threadPoolSize)
			throws UnknownHostException {
		this(InetAddress.getByName(host), port, threadPoolSize);
	}

	ThreadedMessageImporter(InetAddress host, int port, int threadPoolSize) {
		this.host = host;
		this.port = port;
		this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
		directories = new HashSet<String>();
	}

	/**
	 * Recursively descend to all items. If a file is found, and if the
	 * directory in which the file has not already been processed, start a
	 * thread for that directory.
	 * 
	 * @param dir
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void visitAllDirs(File dir) throws MessagingException, IOException {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				visitAllDirs(new File(dir, children[i]));
			}
		} else if (dir.isFile()) {
			final String parentPath = dir.getParentFile().getAbsolutePath();
			if (!directories.contains(parentPath)) {
				directories.add(parentPath);
				System.out.println(parentPath + " submitted");
				threadPool.submit(new FileDirReader(parentPath, host, port));
			}
		}
	}

	public static void main(String args[]) throws MessagingException,
			IOException {
		int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		String path = args[0];
		String host = "localhost";

		if (args.length > 1) {
			String threadPoolSizeStr = args[1];
			threadPoolSize = Integer.parseInt(threadPoolSizeStr);
		}
		System.out.println("thread pool size: " + threadPoolSize);

		if (args.length > 2) {
			host = args[2];
		}

		ThreadedMessageImporter importer = new ThreadedMessageImporter(host,
				CONVERSION_PORT, threadPoolSize);

		long start = System.currentTimeMillis();

		importer.visitAllDirs(new File(path));

		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Finished enqueing/submitting in "
				+ TimeUnit.MILLISECONDS.toMinutes(elapsed) + " minutes "
				+ (TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60) + " seconds");
	}
}
