package com.linuxbox.enkive.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MimeDescriptor;
import org.apache.james.mailbox.model.MessageResult.Header;

public class EnkiveMimeDescriptor implements MimeDescriptor {

	@Override
	public Iterator<Header> headers() throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

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
	public String getMimeType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMimeSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContentMD5() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTransferContentEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisposition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getDispositionParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLines() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getBodyOctets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<MimeDescriptor> parts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MimeDescriptor embeddedMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> contentTypeParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
