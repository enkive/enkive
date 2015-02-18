/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.imap.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;
import org.springframework.util.StringUtils;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class EnkiveImapMessage extends AbstractMessage<String> {

	Message message;
	MessageSummary summary;
	long uid;
	String mailboxId = "";
	String messageId;
	protected boolean messageExists;
	MessageRetrieverService retrieverService;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public EnkiveImapMessage(String messageId,
			MessageRetrieverService retrieverService) {
		this.messageId = messageId;
		this.retrieverService = retrieverService;
		loadSummary();
		if (summary != null)
			messageExists = true;
	}

	@Override
	public Date getInternalDate() {
		loadSummary();
		return summary.getDate();
	}

	@Override
	public String getMailboxId() {
		return mailboxId;
	}

	@Override
	public long getUid() {
		return uid;
	}

	@Override
	public void setUid(long uid) {
		this.uid = uid;
	}

	@Override
	public void setModSeq(long modSeq) {
		// Unused
	}

	@Override
	public long getModSeq() {
		// Unused
		return 0;
	}

	@Override
	public boolean isAnswered() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isDraft() {
		return false;
	}

	@Override
	public boolean isFlagged() {
		return false;
	}

	@Override
	public boolean isRecent() {
		return false;
	}

	@Override
	public boolean isSeen() {
		return true;
	}

	@Override
	public void setFlags(Flags flags) {
		// do nothing; we hard-code our flags
	}

	@Override
	public InputStream getBodyContent() throws IOException {
		ByteArrayInputStream body = new ByteArrayInputStream(message
				.getReconstitutedEmail().getBytes());
		IOUtils.skipFully(body, getBodyStartOctet());
		return body;
	}

	@Override
	public InputStream getFullContent() throws IOException {
		loadMessage();
		return new ByteArrayInputStream(message.getReconstitutedEmail()
				.getBytes());
	}

	@Override
	public String getMediaType() {
		return "";
	}

	@Override
	public String getSubType() {
		return "";
	}

	@Override
	public long getFullContentOctets() {
		loadMessage();
		long length = 0;
		try {
			length = message.getReconstitutedEmail().length();
		} catch (IOException e) {
			LOGGER.error("Error retrieving message with UUID " + messageId, e);
		}
		return length;
	}

	@Override
	public Long getTextualLineCount() {
		loadMessage();
		long lineCount = 0;
		try {
			lineCount = StringUtils.countOccurrencesOf(
					message.getReconstitutedEmail(), "\n");
		} catch (IOException e) {
			LOGGER.error("Error retrieving message with UUID " + messageId, e);
		}
		return lineCount;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		loadSummary();
		return new ByteArrayInputStream(summary.getOriginalHeaders().getBytes());
	}

	@Override
	public List<Property> getProperties() {
		ArrayList<Property> properties = new ArrayList<Property>();
		return properties;
	}

	@Override
	protected int getBodyStartOctet() {
		loadSummary();
		int length = summary.getOriginalHeaders().length();
		return length;
	}

	public Message getMessage() {
		return message;
	}

	/*
	 * does not seem to be needed, especially since messages are retrieved from
	 * back-end
	 * *
	 * public void setMessage(Message message) { this.message = message; }
	 */

	public boolean messageExists() {
		return messageExists;
	}

	private void loadMessage() {
		if (message == null)
			try {
				message = retrieverService.retrieve(messageId);
			} catch (CannotRetrieveException e) {
				LOGGER.error("Error retrieving message with UUID " + messageId);
			} catch (NullPointerException e) {
				LOGGER.warn("Error retrieving message with UUID " + messageId
						+ ". Message may be deleted.");
			}
	}

	private void loadSummary() {
		if (summary == null)
			try {
				summary = retrieverService.retrieveSummary(messageId);
			} catch (CannotRetrieveException e) {
				LOGGER.error("Error retrieving message with UUID " + messageId);
			} catch (NullPointerException e) {
				LOGGER.warn("Error retrieving message with UUID " + messageId
						+ ". Message may be deleted.");
			}
	}
}
