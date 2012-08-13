package com.linuxbox.enkive.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Headers;
import org.apache.james.mailbox.model.MessageResult.Header;

public class EnkiveMessageHeaders implements Headers {

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
	public Iterator<Header> headers() throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

}
