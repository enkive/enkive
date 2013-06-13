/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import java.io.IOException;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.filter.EnkiveFiltersBean;
import com.linuxbox.enkive.message.MessageImpl;

public abstract class AbstractPollClient implements EnkiveClient {
	protected static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.client");
	protected String serviceName;
	protected boolean clientRunning = false;
	
	private final int CONNECT_RETRY_TIME = 120;	// First retry is 2 minutes
	private final int MAX_CONNECT_RETRY_TIME = 86400;	// Retry at least once per day
	private Thread clientThread;
	private MessageArchivingService archiver;
	private EnkiveFiltersBean enkiveFilters;
	
	public AbstractPollClient(String serviceName) {
		super();
		this.serviceName = serviceName;
	}


	/*
	 * Inner Classes
	 */
	
	/**
	 * Client failed to connect.
	 * @author dang
	 */
	public class ClientConnectException extends Exception {
		private static final long serialVersionUID = 1L;
		public ClientConnectException(String message) {
	        super(message);
	    }
	    public ClientConnectException(String message, Throwable throwable) {
	        super(message, throwable);
	    }
	}

	/**
	 * Client disconnected
	 * @author dang
	 */
	public class ClientDisconnectException extends Exception {
		private static final long serialVersionUID = 1L;
		public ClientDisconnectException(String message) {
	        super(message);
	    }
	    public ClientDisconnectException(String message, Throwable throwable) {
	        super(message, throwable);
	    }
	}

	/**
	 * Message was bad, and cannot be used.  This is a non-recoverable error.
	 * @author dang
	 */
	public class ClientBadMessageException extends Exception {
		private static final long serialVersionUID = 1L;
		private final String data;
		public ClientBadMessageException(String message) {
			this(message, "");
	    }
	    public ClientBadMessageException(String message, Throwable throwable) {
			this(message, "", throwable);
	    }
		public ClientBadMessageException(String message, String data) {
			super(message);
			this.data = data;
		}
		public ClientBadMessageException(String message, String data, Throwable throwable) {
			super(message, throwable);
			this.data = data;
		}
		public String getData() {
			return data;
		}
	}

	/**
	 * Message wasn't processed for some reason.  This is a recoverable error,
	 *  and the message should be retried.
	 * @author dang
	 */
	public class ClientMessageProcessException extends Exception {
		private static final long serialVersionUID = 1L;
		private final String data;
		public ClientMessageProcessException(String message) {
			this(message, "");
	    }
	    public ClientMessageProcessException(String message, Throwable throwable) {
			this(message, "", throwable);
	    }
		public ClientMessageProcessException(String message, String data) {
			super(message);
			this.data = data;
		}
		public ClientMessageProcessException(String message, String data, Throwable throwable) {
			super(message, throwable);
			this.data = data;
		}
		public String getData() {
			return data;
		}
	}

	@Override
	public void startClient() {
		if (!clientRunning)
			return;
		
		clientThread = new Thread(this);
		clientThread.setName(serviceName + " client");
		clientThread.start();
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " client started");

	}

	@Override
	public void shutdownClient() {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " client shutdown initiated");
		
		clientRunning = false;
		clientInterrupt();

		if (clientThread != null) {
			try {
				clientThread.join();
			} catch (Exception e) {
				// empty
			}
		}
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " client shutdown completed");

	}
	
	/**
	 * Check it the client should still be running
	 * @return true if should run, false if should exit
	 */
	private boolean clientRunning() {
		return clientRunning;
	}

	@Override
	public void run() {
		int sleepTime = CONNECT_RETRY_TIME;
		
		// Outer loop allows reconnect on timeout
		while (clientRunning()) {
			try {
				clientConnect();
				
				// We got a connection, so reset our back-off
				sleepTime = CONNECT_RETRY_TIME;

				// Inner loop allows exiting when told to exit
				while (clientRunning()) {
					Message msg = getMessage();
					try {
						processMessage(msg);
						messageResult(msg, true);
					} catch (ClientBadMessageException e) {
						LOGGER.error(serviceName + " client got a bad message", e);
						archiver.emergencySave(e.getData(), true);
						// Don't re-process this message
						messageResult(msg, true);
					} catch (ClientMessageProcessException e) {
						LOGGER.error(serviceName + " client failed to save message", e);
						archiver.emergencySave(e.getData(), false);
						// Want to re-process this message
						messageResult(msg, false);
					}
				}
			} catch (ClientConnectException e) {
				LOGGER.error("Failed to connect client.  Backing off", e);
				sleepTime *= 2;
				if (sleepTime > MAX_CONNECT_RETRY_TIME) {
					sleepTime = MAX_CONNECT_RETRY_TIME;
				}
			} catch (ClientDisconnectException e) {
				if (LOGGER.isTraceEnabled())
					LOGGER.trace(serviceName + " client disconnected; retrying", e);
			} catch (FailedToEmergencySaveException e) {
				LOGGER.error("Failed to save emergency file.  Data lost.", e);
				// Get a new connection next time around the loop
				clientDisconnect();
			} catch (AuditServiceException e) {
				LOGGER.error("Failed to save emergency file.  Data lost.", e);
				// Get a new connection next time around the loop
				clientDisconnect();
			}
			
			// Sleep so as to not pound the server with retries
			clientSleep(sleepTime);
		}

		clientDisconnect();
	}
	
	private void processMessage(Message msg) throws
		ClientBadMessageException, ClientMessageProcessException {
		String data = null;
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			msg.writeTo(output);
			data = output.toString();
			MessageImpl emsg = new MessageImpl(data);
			setAddresses(emsg, msg);
			boolean archiveMessage = enkiveFilters.filterMessage(emsg);

			if (archiveMessage) {
				String messageUUID = archiver.storeOrFindMessage(emsg);
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Message: " + emsg.getCleanMessageId()
							+ " successfully archived with UUID " + messageUUID);
			} else {
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Message Rejected:" + emsg.getMessageId()
							+ " did not pass message filters");
			}
		} catch (CannotTransferMessageContentException e) {
			throw new ClientMessageProcessException("Archive failed: ", data, e);
		} catch (IOException e) {
			throw new ClientMessageProcessException("Archive failed: ", data, e);
		} catch (MessagingException e) {
			throw new ClientMessageProcessException("Archive failed: ", data, e);
		} catch (Exception e) {
			throw new ClientBadMessageException("Archive failed: ", data, e);
		}
	}

	/**
	 * Set the from and rcptto addresses for the enkive message from the javamail message.
	 * @param emsg	Enkive message to set on
	 * @param msg	JavaMail message to get from
	 * @throws MessagingException
	 */
	private void setAddresses(MessageImpl emsg, Message msg) throws MessagingException {
		Address[] addrs;

		addrs = msg.getFrom();
		if (addrs.length <= 0) {
			throw new MessagingException("No from address");
		}
		emsg.setMailFrom(addrs[0].toString());

		addrs = msg.getAllRecipients();
		for (int i = 0; i < addrs.length; i++) {
			emsg.appendRcptTo(addrs[i].toString());
		}
	}

	public void setEnkiveFilters(EnkiveFiltersBean filters) {
		enkiveFilters = filters;
	}

	public void setArchiver(MessageArchivingService archiver) {
		this.archiver = archiver;
	}

	/* Subclass API */
	
	/**
	 * Connect this client to it's source.
	 * 
	 * @throws ClientConnectException
	 */
	protected abstract void clientConnect() throws ClientConnectException;

	/**
	 * Disconnect this client to it's source.  It's not an error if the
	 * client was never connected.
	 */
	protected abstract void clientDisconnect();
	
	/**
	 * Sleep for the configured timeout.
	 */
	protected abstract void clientSleep(int seconds);
	
	/**
	 * Interrupt the (potentially sleeping) client.
	 */
	protected abstract void clientInterrupt();
	
	/**
	 * Get the next message from the server.
	 * 
	 * @throws ClientDisconnectException
	 */
	protected abstract Message getMessage() throws ClientDisconnectException;
	
	/**
	 * Notify subclass whether or not the message was archived properly
	 * 
	 * @throws ClientDisconnectException
	 */
	protected abstract void messageResult(Message msg, boolean status)
			throws ClientDisconnectException;

}
