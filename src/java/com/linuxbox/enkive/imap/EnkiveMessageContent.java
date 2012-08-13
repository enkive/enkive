package com.linuxbox.enkive.imap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Content;

public class EnkiveMessageContent implements Content {

	String testData = "TESTING BODY";
	
	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(testData.getBytes());
	}

	@Override
	public long size() throws MailboxException {
		// TODO Auto-generated method stub
		return testData.length();
	}

}
