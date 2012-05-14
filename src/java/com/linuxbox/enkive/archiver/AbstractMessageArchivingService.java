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
package com.linuxbox.enkive.archiver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileLock;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.GeneralConstants;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.archiver.exceptions.MessageArchivingServiceException;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.audit.AuditTrailException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.message.Message;

public abstract class AbstractMessageArchivingService implements
		MessageArchivingService {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.messageArchvingService");
	private final static int EMERGENCY_SAVE_ATTEMPTS = 4;

	/**
	 * When an emergency save file is created, it's given a name with a
	 * timestamp plus a random 8-digit hex value to avoid naming conflicts.
	 * Should be at least 268,435,547 but less than 2,147,483,648 to fit.
	 */
	private final static long RANDOM_FILENAME_SUFFIX_RANGE = 2147480000;

	protected DocStoreService docStoreService;
	protected String emergencySaveRoot;
	protected AuditService auditService;

	@SuppressWarnings("serial")
	class SaveFileAlreadyExistsException extends FailedToEmergencySaveException {
		public SaveFileAlreadyExistsException(String message) {
			super(message);
		}
	}

	protected abstract void subStartup()
			throws MessageArchivingServiceException;

	protected abstract void subShutdown()
			throws MessageArchivingServiceException;

	public void startup() throws MessageArchivingServiceException {
		if (docStoreService == null) {
			throw new MessageArchivingServiceException(
					"DocStore service not set");
		}

		subStartup();
	}

	public void shutdown() throws MessageArchivingServiceException {
		subShutdown();
	}

	public String storeOrFindMessage(Message message)
			throws CannotArchiveException, FailedToEmergencySaveException,
			AuditServiceException, IOException {
		String uuid = null;
		try {
			uuid = findMessage(message);
			if (uuid == null) {
				uuid = storeMessage(message);
			}
		} catch (Exception e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(
						"Could not archive Message "
								+ message.getCleanMessageId(), e);
			emergencySave(message.getReconstitutedEmail());
		}
		return uuid;
	}

	public DocStoreService getDocStoreService() {
		return docStoreService;
	}

	public void setDocStoreService(DocStoreService docStoreService) {
		this.docStoreService = docStoreService;
	}

	public static String calculateMessageId(Message message)
			throws CannotArchiveException {
		String messageUUID = null;
		try {
			MessageDigest sha1calc = MessageDigest.getInstance("SHA-1");
			sha1calc.reset();
			messageUUID = new String((new Hex()).encode(sha1calc.digest(message
					.getReconstitutedEmail().getBytes())));
		} catch (NoSuchAlgorithmException e) {
			throw new CannotArchiveException(
					"Could not calculate UUID for message", e);
		} catch (IOException e) {
			throw new CannotArchiveException(
					"Could not calculate UUID for message", e);
		}
		return messageUUID;
	}

	/**
	 * Convenience method assumes the message is not incomplete.
	 * 
	 * @param data
	 * @throws FailedToEmergencySaveException
	 * @throws AuditTrailException
	 */
	public boolean emergencySave(final String data)
			throws FailedToEmergencySaveException, AuditServiceException {
		return emergencySave(data, false);
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
	public boolean emergencySave(final String data, boolean messageIsIncomplete)
			throws FailedToEmergencySaveException, AuditServiceException {
		boolean messageSaved = false;
		if (!data.isEmpty()) {
			final String fileName = saveToDisk(data, messageIsIncomplete);
			auditService.addEvent(AuditService.MESSAGE_EMERGENCY_SAVED,
					AuditService.USER_SYSTEM, fileName);
			if (!fileName.isEmpty() && !messageIsIncomplete)
				messageSaved = true;

		} else {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("emergency save data is empty; nothing saved");
		}
		return messageSaved;
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
			String random = String.format("%08x",
					Math.round(Math.random() * RANDOM_FILENAME_SUFFIX_RANGE));

			Date now = Calendar
					.getInstance(GeneralConstants.STANDARD_TIME_ZONE).getTime();

			String fileName = GeneralConstants.NUMERIC_FORMAT_W_MILLIS
					.format(now) + "_" + random;
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
		final String filePath = getEmergencySaveRoot() + "/" + fileName;

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

				if (LOGGER.isInfoEnabled())
					LOGGER.info("Saved message to file: \"" + fileName + "\"");
				return filePath;
			} finally {
				if (lock != null) {
					lock.release();
				}
			}
		} catch (IOException e) {
			if (LOGGER.isFatalEnabled())
				LOGGER.fatal("Emergency save to disk failed. ", e);
			throw new FailedToEmergencySaveException(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				} else if (fileStream != null) {
					fileStream.close();
				}
			} catch (IOException e) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("could not close emergency save file \""
							+ filePath + "\".");
			}
		}
	}

	public String getEmergencySaveRoot() {
		return emergencySaveRoot;
	}

	public void setEmergencySaveRoot(String emergencySaveRoot) {
		this.emergencySaveRoot = emergencySaveRoot;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
