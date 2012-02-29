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
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.Provider;
import javax.mail.URLName;

public class MboxReader extends AbstractMailboxImporter {

	static {
		try {
			Class.forName("gnu.mail.providers.mbox.MboxStore");
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		}
	}

	MboxReader(String rootDir, String host, String port)
			throws MessagingException, IOException {
		super(rootDir, host, port, new URLName(new String("mbox://" + rootDir)));

	}

	@Override
	public void readAllMessages() throws MessagingException, IOException {
		File dir = new File(rootDir);
		for (File mboxFile : dir.listFiles())
			readMailDirectory(mboxFile.getName());
	}

	// Had to include gnumail, gnumailproviders, and inetlib in classpath
	public static void main(String args[]) {
		if (args.length != 3) {
			System.err
					.println("Error: requires command-line arguments representing path, host, and port number");
			System.exit(1);
		}

		MboxReader reader = null;

		final String path = args[0]; // path to home folder to Archive
		final String host = args[1];
		final String portString = args[2];

		long startTime = System.currentTimeMillis();
		try {
			reader = new MboxReader(path, host, portString);
			reader.setWriter();
			reader.readAllMessages();
			reader.closeWriter();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		} finally {
			long elapsedTime = System.currentTimeMillis() - startTime;
			System.out.println();
			System.out.println(reader.getMessageCount()
					+ " messages processed in "
					+ TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
					+ " minutes "
					+ (TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60)
					+ " seconds");
		}
	}

	@Override
	protected void setupSession() {
		Provider mboxProvider = new Provider(Provider.Type.STORE, "mbox",
				"gnu.mail.providers.mbox.MboxStore", "gnumail", "1");
		session.addProvider(mboxProvider);
	}
}
