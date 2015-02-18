/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.client;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.linuxbox.enkive.client.config.MailPollClientConfiguration;
import com.linuxbox.util.InterruptableSleeper;

public class MailPollClient extends AbstractPollClient {
	private MailPollClientConfiguration config;
	private Session session;
	private Store store;
	private Folder folder;
	private boolean debug = false;
	private InterruptableSleeper sleeper;

	public MailPollClient() {
		super("MailPoll");
		sleeper = new InterruptableSleeper();
		sleeper.start();
	}

	public MailPollClient(MailPollClientConfiguration config) {
		this();
		this.setConfiguration(config);
	}
	
	public MailPollClientConfiguration getConfiguration() {
		return config;
	}
	public void setConfiguration(MailPollClientConfiguration config) {
		this.config = config;
		if (config.isEnabled())
			this.clientRunning = true;
	}

	/**
	 * Connect to the mail server represented by the configuration
	 */
	@Override
	protected void clientConnect() throws ClientConnectException {
		Properties props = System.getProperties();

		session = Session.getInstance(props, null);
		session.setDebug(this.debug);

		try {
			store = session.getStore(config.getURL());
			store.connect();
			folder = store.getFolder(config.getFolder());
			folder.open(Folder.READ_WRITE);
		} catch (MessagingException e) {
			throw new ClientConnectException("Failed to connect to client " + serviceName, e);
		}
	}
	
	/**
	 * Disconnect from the server and clean up
	 */
	@Override
	protected void clientDisconnect() {
		try {
			folder.close(false);
			store.close();
		} catch (MessagingException e) {
			// Nothing to do
		} catch (NullPointerException e) {
			// No folder, it's fine
		}
	}

	/**
	 * Zzzzz...
	 */
	@Override
	protected void clientSleep(int seconds) {
		sleeper.waitFor(seconds * 1000);
	}
	
	/**
	 * Interrupt our sleep
	 */
	@Override
	protected void clientInterrupt() {
		sleeper.interrupt();
	}
	
	@Override
	protected Message getMessage() throws ClientDisconnectException {
		Message msg;
		
		try {
			int messageCount = folder.getMessageCount();

			if (messageCount == -1) {
				throw new ClientDisconnectException(serviceName + ": Folder is closed");
			}
			
			while (messageCount == 0) {
				sleeper.waitFor(config.getTimeout() * 1000);
				if (sleeper.wasInterrupted()) {
					throw new ClientDisconnectException(serviceName + ": Interrupted");
				}
				messageCount = folder.getMessageCount();
				if (messageCount == -1) {
					throw new ClientDisconnectException(serviceName + ": Folder is closed");
				}
			}

			// There's at least one message.  Get the first one, and mark it deleted.
			msg = folder.getMessage(1);

			folder.setFlags(1, 1, new Flags(Flags.Flag.DELETED), true);
		} catch (MessagingException e) {
			throw new ClientDisconnectException(serviceName + ": Message operation failed for", e);
		}
		
		return msg;
	}

	@Override
	protected void messageResult(Message msg, boolean status)
			throws ClientDisconnectException {
		try {
			if (status) {
				folder.expunge();
			} else {
				folder.setFlags(1, 1, new Flags(Flags.Flag.DELETED), false);
			}
		} catch (MessagingException e) {
			throw new ClientDisconnectException(serviceName + ": couldn't clean up message", e);
		}
	}

}
