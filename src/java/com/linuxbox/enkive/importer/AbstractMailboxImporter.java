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
 *******************************************************************************/
package com.linuxbox.enkive.importer;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public abstract class AbstractMailboxImporter extends AbstractMessageImporter {

	protected String rootDir;
	protected Session session;
	protected Folder root;
	protected Store store;

	AbstractMailboxImporter(String rootDir, String host, String port,
			URLName url) throws UnknownHostException, MessagingException {
		super(host, port);
		this.rootDir = rootDir + "/";
		session = Session.getDefaultInstance(System.getProperties());

		setupSession();

		store = session.getStore(url);
		store.connect();
		root = store.getDefaultFolder();
	}

	public void readMailDirectory(String subDir) throws MessagingException,
			IOException {
		Folder inbox = root.getFolder(subDir);
		readMailDirectory(inbox);
	}

	public void readMailDirectory(Folder folder) throws MessagingException,
			IOException {
		System.out.println(folder.getName() + " - Starting - "
				+ folder.getMessageCount() + " Messages");
		folder.open(Folder.READ_ONLY);
		for (Message m : folder.getMessages()) {
			sendMessage(m);
			if (messageCount % 100 == 0) {
				System.out.println(messageCount + " Messages Sent");
			}
		}
		folder.close(false);
		System.out.println(folder.getName() + " - Finished");
	}

	protected abstract void readAllMessages() throws MessagingException,
			IOException;

	protected abstract void setupSession();

}
