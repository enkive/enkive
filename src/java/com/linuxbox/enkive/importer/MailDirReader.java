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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.URLName;

public class MailDirReader extends AbstractMailboxImporter {

	private Set<String> ignoreDirectories;
	public int messageCount = 0;

	static {
		try {
			Class.forName("gnu.mail.providers.maildir.MaildirStore");
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		}
	}

	MailDirReader(String rootDir, String host, String port)
			throws MessagingException, IOException {
		super(rootDir, host, port, new URLName("maildir://" + rootDir));
		ignoreDirectories = new HashSet<String>();

		System.setProperty("user.home", rootDir);
	}

	@Override
	public void readAllMessages() throws MessagingException, IOException {
		System.out.print(messageCount);
		readMailDirectory("INBOX");
		// now process any subdirectories
		String[] subDirs = listSubDirectories();
		if (subDirs != null) {
			for (String subDir : listSubDirectories()) {
				readMailDirectory(subDir);
			}
		}
	}

	public String[] listSubDirectories() {
		File dir = new File(rootDir);
		FilenameFilter filenameFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return (name.startsWith(".") && dir.isDirectory() && !ignoreDirectories
						.contains(name));
			}
		};
		return dir.list(filenameFilter);
	}

	public void addIgnoreDirectory(String subDir) {
		ignoreDirectories.add(subDir);
	}

	public void removeIgnoreDirectory(String subDir) {
		ignoreDirectories.remove(subDir);
	}

	public String[] listIgnoreDirectories() {
		return ignoreDirectories.toArray(new String[0]);
	}

	// Had to include gnumail, gnumailproviders, and inetlib in classpath
	public static void main(String args[]) {
		if (args.length != 3) {
			System.err
					.println("Error: requires command-line arguments representing path, host, and port number");
			System.exit(1);
		}
		MailDirReader reader = null;

		final String path = args[0]; // path to home folder to Archive
		final String host = args[1];
		final String portString = args[2];

		long startTime = System.currentTimeMillis();
		try {
			reader = new MailDirReader(path, host, portString);
			reader.setWriter();
			reader.addIgnoreDirectory(".Trash");
			reader.addIgnoreDirectory(".Trash.2011");
			reader.addIgnoreDirectory(".Drafts");

			reader.readAllMessages();
			reader.closeWriter();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println();
			System.out.println(reader.messageCount + " messages processed in "
					+ TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
					+ " minutes "
					+ (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60)
					+ " seconds");
		}
	}
}
