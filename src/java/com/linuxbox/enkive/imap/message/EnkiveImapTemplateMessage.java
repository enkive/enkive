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

import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class EnkiveImapTemplateMessage extends AbstractMessage<String> {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream getBodyContent() throws IOException {
		StringWriter stringWriter = new StringWriter();
		try {
			messageBody.process(root, stringWriter);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer message = stringWriter.getBuffer();
		return new ByteArrayInputStream(message.toString().getBytes());
	}

	@Override
	public String getMediaType() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String getSubType() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public long getFullContentOctets() {
		StringWriter stringWriter = new StringWriter();
		try {
			messageBody.process(root, stringWriter);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		// TODO Auto-generated method stub
		return new ArrayList<Property>();
	}

	@Override
	protected int getBodyStartOctet() {
		return headers.length();
	}

}
