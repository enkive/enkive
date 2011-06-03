/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.mailprocessor;

import static com.linuxbox.enkive.mailprocessor.ProcessorState.ANALYZING_MESSAGE_DATA;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.ARCHIVING;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.EMERGENCY_SAVING;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.ERROR_HANDLING;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.IDLE;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.PARSING_MESSAGE;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.POST_PROCESSING_MESSAGE;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.PREPARING_PROCESSOR;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.RETRIEVING_MESSAGE_DATA;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.SHUTTING_DOWN;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.STARTING_UP_ARCHIVER;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.FileLock;
import java.util.Calendar;
import java.util.Date;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.MimeException;

import com.linuxbox.enkive.GeneralConstants;
import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.MessageArchivingServiceException;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.audit.AuditTrailException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.exception.SocketClosedException;
import com.linuxbox.enkive.exception.UninitializedMailProcessorException;
import com.linuxbox.enkive.filter.EnkiveFiltersBean;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.server.AbstractSocketServer;
import com.linuxbox.util.MBeanUtils;

public abstract class AbstractMailProcessor implements ArchivingProcessor,
		AbstractMailProcessorMBean {
	protected final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.mailprocessor");
	private final static int EMERGENCY_SAVE_ATTEMPTS = 4;

	/**
	 * When an emergency save file is created, it's given a name with a
	 * timestamp plus a random 8-digit hex value to avoid naming conflicts.
	 * Should be at least 268,435,547 but less than 2,147,483,648 to fit.
	 */
	private final static long RANDOM_FILENAME_SUFFIX_RANGE = 2147480000;

	protected Socket socket;
	protected AbstractSocketServer server;
	protected MessageArchivingService archiver;
	protected AuditService auditService;
	
	protected EnkiveFiltersBean enkiveFilters;
	protected String emergencySaveRoot;
	protected boolean jmxEnabled = false;
	
	private boolean closeInitiated;
	private boolean initialized;
	protected boolean multiMessage = false;
	protected boolean processingComplete = false;
	protected boolean messageSaved = false;

	private ObjectName mBeanName;
	protected ProcessorState processorState = IDLE;
	private int messagesProcessed = 0;
	private long totalProcessingMilliseconds = 0;

	/*
	 * Inner Classes
	 */

	@SuppressWarnings("serial")
	class FailedToEmergencySaveException extends Exception {
		public FailedToEmergencySaveException(String message) {
			super(message);
		}

		public FailedToEmergencySaveException(Exception e) {
			super(e);
		}

		public FailedToEmergencySaveException(String message, Exception e) {
			super(message, e);
		}
	}

	@SuppressWarnings("serial")
	class SaveFileAlreadyExistsException extends FailedToEmergencySaveException {
		public SaveFileAlreadyExistsException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	protected class MessageIncompleteException extends Exception {
		private final String data;

		public MessageIncompleteException(final String message) {
			this(message, "");
		}

		public MessageIncompleteException(final String message,
				final String data) {
			super(message);
			this.data = data;
		}

		public String getData() {
			return data;
		}
	}

	/*
	 * AbstractMailProcessor methods
	 */

	public AbstractMailProcessor() {
		super();
	}

	@Override
	public void initializeProcessor(AbstractSocketServer server, Socket socket) {
		logger.trace("in initializeProcessor");
		this.server = server;
		this.socket = socket;

		closeInitiated = false;

		// set the socket linger options, so the socket closes immediately when
		// closed
		try {
			socket.setSoLinger(false, 0);
		} catch (SocketException e) {
			logger.debug("enkive session unable to set socket linger " + e);
		}

		if (isJmxEnabled()) {
			String type = getClass().getSimpleName();
			String name = "Port " + socket.getPort();

			mBeanName = MBeanUtils.registerMBean(this, type, name);
			logger.trace("registered mbean " + mBeanName + " (" + type + "/"
					+ name + ")");
		}

		initialized = true;
	}

	public void initiateStop() {
		logger.trace("in stopProcessor");
		closeInitiated = true;
		closeSessionResources();
		try {
			archiver.shutdown();
		} catch (MessageArchivingServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (!initialized) {
			throw new UninitializedMailProcessorException();
		}

		try {
			processorState = STARTING_UP_ARCHIVER;
			archiver.startup();

			processorState = PREPARING_PROCESSOR;
			prepareProcessor();

			do {
				long startTime = System.currentTimeMillis();
				Message message = null;
				String data = "";

				try {
					processorState = RETRIEVING_MESSAGE_DATA;
					data = processInput();

					processorState = ANALYZING_MESSAGE_DATA;
					if (!data.isEmpty()) {
						processorState = PARSING_MESSAGE;
						message = createMessage(data);

						processorState = POST_PROCESSING_MESSAGE;
						message = postProcess(message);

						processorState = ARCHIVING;
						archiveMessage(message);
					}
				} catch (SocketClosedException e) {
					// just pass this up to the next level
					throw e;
				} catch (MessageIncompleteException e) {
					processorState = ERROR_HANDLING;
					logger.fatal(
							"socket closed with only partial message read", e);
					handleEmergencySaving(e.getData(), true);
					processingComplete = true;
				} catch (BadMessageException e) {
					processorState = ERROR_HANDLING;
					logger.fatal("could not create message object to archive",
							e);
					handleEmergencySaving(data);
				} catch (Exception e) {
					processorState = ERROR_HANDLING;
					logger.fatal("could not archive message", e);
					handleEmergencySaving(data);
				}

				processorState = IDLE;
				data = "";

				++messagesProcessed;
				long endTime = System.currentTimeMillis();
				totalProcessingMilliseconds += (endTime - startTime);
			} while (multiMessage && !processingComplete);
		} catch (SocketClosedException e) {
			// do nothing, as this is a normal outcome
		} catch (Exception e) {
			processorState = ERROR_HANDLING;
			messageSaved = false;
			logger.error("Message archival preparation failure", e);
			try {
				auditService.addEvent(AuditService.MESSAGE_ARCHIVE_FAILURE,
						AuditService.USER_SYSTEM,
						"Failed in pre-archival steps");
			} catch (AuditServiceException e2) {
				logger.fatal("failure to audit archiving failure", e2);
			}
		}

		processorState = SHUTTING_DOWN;
		if (!closeInitiated) {
			closeProcessor();
			closeSessionResources();
			try {
				archiver.shutdown();
			} catch (MessageArchivingServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			server.processorClosed(this);
		}

		logger.trace("closed session");

		MBeanUtils.unregisterMBean(mBeanName);
		logger.trace("unregistered mbean " + mBeanName);
	}

	public String getProcessorState() {
		return processorState.toString();
	}

	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public double getMillisecondsPerMessage() {
		if (messagesProcessed == 0) {
			// avoid NAN for result
			return 0.0;
		} else {
			return totalProcessingMilliseconds / (double) messagesProcessed;
		}
	}

	public boolean isClosed() {
		return socket == null;
	}

	protected void closeSessionResources() {
		logger.trace("in closeSessionResources");

		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				// empty
			}
			socket = null;
		}
	}

	/**
	 * Convenience method assumes the message is not incomplete.
	 * 
	 * @param data
	 * @throws FailedToEmergencySaveException
	 * @throws AuditTrailException
	 */
	private void handleEmergencySaving(final String data)
			throws FailedToEmergencySaveException, AuditServiceException {
		handleEmergencySaving(data, false);
	}

	/**
	 * Contains basic logic for doing an emergency save since this needs to be
	 * done from a few points in the code.
	 * 
	 * @param data
	 * @param messageIsIncomplete
	 * @throws FailedToEmergencySaveException
	 * @throws AuditTrailException
	 * @throws AuditServiceException 
	 */
	private void handleEmergencySaving(final String data,
			boolean messageIsIncomplete) throws FailedToEmergencySaveException,
			AuditServiceException {
		if (!data.isEmpty()) {
			processorState = EMERGENCY_SAVING;
			final String fileName = saveToDisk(data, messageIsIncomplete);
			auditService.addEvent(AuditService.MESSAGE_EMERGENCY_SAVED,
					AuditService.USER_SYSTEM, fileName);
		} else {
			logger.warn("emergency save data is empty; nothing saved");
		}
	}

	/**
	 * Generates a file name based on the current date and time, including
	 * milliseconds. It's unlikely that another save file would occur during the
	 * same millisecond. But as added protection a random number (over 30 bits)
	 * is appended as well (in base 16).
	 * 
	 * @param data
	 * @throws IOException
	 */
	private String saveToDisk(String data, boolean messageIsIncomplete)
			throws FailedToEmergencySaveException {
		SaveFileAlreadyExistsException lastExistsException = null;

		for (int attempt = 0; attempt < EMERGENCY_SAVE_ATTEMPTS; ++attempt) {
			// choose one of two billion random numbers
			String random = String.format("%08x", Math.round(Math.random()
					* RANDOM_FILENAME_SUFFIX_RANGE));

			Date now = Calendar
					.getInstance(GeneralConstants.STANDARD_TIME_ZONE).getTime();

			String fileName = GeneralConstants.NUMERIC_FORMAT_W_MILLIS
					.format(now)
					+ "_" + random;
			if (messageIsIncomplete) {
				fileName += "-incomplete";
			}
			fileName += ".eml";

			try {
				saveToDisk(data, fileName);
				return fileName;
			} catch (SaveFileAlreadyExistsException e) {
				// empty ; loop again
			}
		}

		// throw the last exception encapsulated in a
		// FailedToEmergencySaveException
		throw new FailedToEmergencySaveException(lastExistsException);
	}

	private String saveToDisk(String messageData, String fileName)
			throws FailedToEmergencySaveException,
			SaveFileAlreadyExistsException {
		final String filePath = getEmergencySaveRoot() + "/"
				+ fileName;

		BufferedWriter out = null;
		FileOutputStream fileStream = null;
		try {
			fileStream = new FileOutputStream(filePath);
			FileLock lock = null;
			try {
				lock = fileStream.getChannel().tryLock();
				if (lock == null) {
					throw new SaveFileAlreadyExistsException(filePath);
				}

				out = new BufferedWriter(new OutputStreamWriter(fileStream));
				out.write(messageData);

				logger.info("Saved message to file: \"" + fileName + "\"");
				messageSaved = true;
				return filePath;
			} finally {
				if (lock != null) {
					lock.release();
				}
			}
		} catch (IOException e) {
			logger.fatal("Emergency save to disk failed. ", e);
			throw new FailedToEmergencySaveException(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				} else if (fileStream != null) {
					fileStream.close();
				}
			} catch (IOException e) {
				logger.warn("could not close emergency save file \"" + filePath
						+ "\".");
			}
		}
	}

	public String getEmergencySaveRoot() {
		return emergencySaveRoot;
	}
	
	public void setEmergencySaveRoot(String emergencySaveRoot) {
		this.emergencySaveRoot = emergencySaveRoot;
	}

	private Message createMessage(String data) throws IOException,
			BadMessageException {
		try {
			return new MessageImpl(data);
		} catch (BadMessageException e) {
			logger.error("unable to archive", e);
			throw new BadMessageException(e);
		} catch (CannotTransferMessageContentException e) {
			throw new BadMessageException(e);
		} catch (MimeException e) {
			throw new BadMessageException(e);
		}
	}

	private void archiveMessage(Message message) throws IOException,
			CannotArchiveException {
		boolean archiveMessage = enkiveFilters.filterMessage(message);

		if (archiveMessage) {
			String messageUUID = archiver.storeOrFindMessage(message);
				logger.info("Message: " + message.getCleanMessageId()
						+ " successfully archived with UUID " + messageUUID);
				messageSaved = true;
		} else {
			logger.info("Message Rejected:" + message.getMessageId()
					+ " did not pass message filters");
			messageSaved = true;
		}
	}
	
	public void setEnkiveFilters(EnkiveFiltersBean filters){
		enkiveFilters = filters;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public MessageArchivingService getArchiver() {
		return archiver;
	}

	public void setArchiver(MessageArchivingService archiver) {
		this.archiver = archiver;
	}

	public boolean isJmxEnabled() {
		return jmxEnabled;
	}
	
	public void setJmxEnabled(boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}
	
	/*
	 * Subclass API
	 */

	/**
	 * Prepares the processor. The processor may, for example, want to create an
	 * OutputStreamWriter from the OutputStream.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	protected abstract void prepareProcessor() throws IOException;

	/**
	 * Method responsible for obtaining the Message and returning it as a
	 * string. Must either set the data string or the message object
	 * 
	 * @throws SocketClosedException
	 * @throws IOException
	 * @throws MimeException
	 * @throws BadMessageException
	 * @throws CannotTransferMessageContentException
	 */
	protected abstract String processInput() throws MessageIncompleteException,
			SocketClosedException, IOException;

	/**
	 * Any processing of the message object that needs to be done before
	 * archiving is done here.
	 */
	protected abstract Message postProcess(Message message);

	/**
	 * This method should close any writers or additional sockets that were
	 * opened in prepareProcessor().
	 */
	protected abstract void closeProcessor();
}