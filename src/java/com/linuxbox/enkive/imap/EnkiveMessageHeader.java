package com.linuxbox.enkive.imap;

import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageResult;

public class EnkiveMessageHeader implements MessageResult.Header {

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() throws MailboxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue() throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

}
