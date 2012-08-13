package com.linuxbox.enkive.imap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.mail.Flags;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Content;
import org.apache.james.mailbox.model.Headers;
import org.apache.james.mailbox.model.MessageResult;
import org.apache.james.mailbox.model.MimeDescriptor;

public class EnkiveMessageResult implements MessageResult {

	ArrayList<Header> headers;
	Content bodyContent;
	
	public EnkiveMessageResult(){
		ArrayList<Header> headers = new ArrayList<Header>();
		headers.add(new EnkiveMessageHeader());
		bodyContent = new EnkiveMessageContent();
	}
	
	@Override
	public int compareTo(MessageResult o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getUid() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public long getModSeq() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Flags getFlags() {
		return new Flags();
		// TODO Auto-generated method stub
	}

	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Date getInternalDate() {
		// TODO Auto-generated method stub
		return new Date();
	}

	@Override
	public MimeDescriptor getMimeDescriptor() throws MailboxException {
		MimeDescriptor descriptor = new EnkiveMimeDescriptor();
		// TODO Auto-generated method stub
		return descriptor;
	}

	@Override
	public Iterator<Header> iterateHeaders(MimePath path)
			throws MailboxException {
		return headers.iterator();
		// TODO Auto-generated method stub
	}

	@Override
	public Iterator<Header> iterateMimeHeaders(MimePath path)
			throws MailboxException {
		// TODO Auto-generated method stub
		return headers.iterator();
	}

	@Override
	public Content getFullContent() throws MailboxException, IOException {
		// TODO Auto-generated method stub
		return bodyContent;
	}

	@Override
	public Content getFullContent(MimePath path) throws MailboxException {
		// TODO Auto-generated method stub
		return bodyContent;
	}

	@Override
	public Content getBody() throws MailboxException, IOException {
		// TODO Auto-generated method stub
		return bodyContent;
	}

	@Override
	public Content getBody(MimePath path) throws MailboxException {
		// TODO Auto-generated method stub
		return bodyContent;
	}

	@Override
	public Content getMimeBody(MimePath path) throws MailboxException {
		// TODO Auto-generated method stub
		return bodyContent;
	}

	@Override
	public Headers getHeaders() throws MailboxException {
		// TODO Auto-generated method stub
		return new EnkiveMessageHeaders();
	}

}
