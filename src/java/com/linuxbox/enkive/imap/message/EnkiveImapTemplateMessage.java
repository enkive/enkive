/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class EnkiveImapTemplateMessage extends AbstractMessage<String> {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	Template messageBody;
	Map<String, Object> root = new HashMap<String, Object>();

	String headers = "From: enkive@enkive.org\r\nTo: enkive@enkive.org\r\nSubject: Welcome to the Enkive IMAP Interface!\r\n\r\n";

	public EnkiveImapTemplateMessage(String pathToTemplate) {
		Configuration cfg = new Configuration();
		File templatesDirectory = new File("config/templates");
		try {
			cfg.setDirectoryForTemplateLoading(templatesDirectory);
			messageBody = cfg.getTemplate(pathToTemplate);
		} catch (IOException e) {
			LOGGER.error("Error producing IMAP Inbox message from template", e);
		}
	}

	@Override
	public Date getInternalDate() {
		return new Date();
	}

	@Override
	public String getMailboxId() {
		return "INBOX";
	}

	@Override
	public long getUid() {
		return 1;
	}

	@Override
	public void setUid(long uid) {

	}

	@Override
	public void setModSeq(long modSeq) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getModSeq() {
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

	}

	@Override
	public InputStream getBodyContent() throws IOException {
		StringWriter stringWriter = new StringWriter();
		try {
			messageBody.process(root, stringWriter);
		} catch (TemplateException e) {
			LOGGER.error("Error producing IMAP Inbox message from template", e);
		}
		StringBuffer message = stringWriter.getBuffer();
		return new ByteArrayInputStream(message.toString().getBytes());
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
		StringWriter stringWriter = new StringWriter();
		try {
			messageBody.process(root, stringWriter);
		} catch (TemplateException e) {
			LOGGER.error("Error producing IMAP Inbox message from template", e);
		} catch (IOException e) {
			LOGGER.error("Error producing IMAP Inbox message from template", e);
		}
		StringBuffer message = stringWriter.getBuffer();
		return headers.length() + message.length();
	}

	@Override
	public Long getTextualLineCount() {
		// TODO Auto-generated method stub
		return (long) 8;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		return new ByteArrayInputStream(headers.getBytes());
	}

	@Override
	public List<Property> getProperties() {
		return new ArrayList<Property>();
	}

	@Override
	protected int getBodyStartOctet() {
		return headers.length();
	}

}
