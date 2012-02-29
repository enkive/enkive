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

import javax.mail.MessagingException;

public class FileDirReader extends AbstractMessageImporter implements Runnable {

	private File rootDir;

	FileDirReader(String rootDir, String host, String port)
			throws MessagingException, IOException {
		super(host, port);
		this.rootDir = new File(rootDir);
	}

	FileDirReader(String rootDir, String host, int port)
			throws MessagingException, IOException {
		super(InetAddress.getByName(host), port);
		this.rootDir = new File(rootDir);
	}

	FileDirReader(String rootDir, InetAddress host, int port)
			throws MessagingException, IOException {
		super(host, port);
		this.rootDir = new File(rootDir);
	}

	FileDirReader(File rootDir, InetAddress host, int port)
			throws MessagingException, IOException {
		super(host, port);
		this.rootDir = rootDir;
	}

	// Process only files in dir
	public void sendAllFiles(File dir) throws IOException, MessagingException {
		if (dir.isDirectory()) {
			System.out.println(dir.getAbsolutePath() + " - Started");
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					sendMessage(file);
				} else {
					sendAllFiles(file);
				}
			}
			System.out.println(dir.getAbsolutePath() + " - Finished without error");
		}
	}

	@Override
	public void run() {
		try {
			setWriter();
			System.out.println(rootDir.getName() + " - Started");
			sendAllFiles(rootDir);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} finally {
			closeWriter();
		}
		System.out.println(rootDir.getName() + " - Done");
	}

	public static void main(String args[]) throws MessagingException,
			IOException {
		FileDirReader reader = new FileDirReader(args[0], args[1], args[2]);
		reader.run();
		System.out.println(reader.getMessageCount() + " Messages imported");
	}
}
