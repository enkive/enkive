package com.linuxbox.enkive.imap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;

public class EnkiveImapMessage extends AbstractMessage<Long> {

	public EnkiveImapMessage(){
		
	}
	
	@Override
	public Date getInternalDate() {
		// TODO Auto-generated method stub
		return new Date();
	}

	@Override
	public Long getMailboxId() {
		// TODO Auto-generated method stub
		return (long) 1;
	}

	@Override
	public long getUid() {
		// TODO Auto-generated method stub
		return (long) 1;
	}

	@Override
	public void setUid(long uid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setModSeq(long modSeq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getModSeq() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAnswered() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDeleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDraft() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFlagged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRecent() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isSeen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFlags(Flags flags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputStream getBodyContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream("TESTBODY".getBytes());
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getTextualLineCount() {
		// TODO Auto-generated method stub
		return (long) 1;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream("Subject: TEST".getBytes());
	}

	@Override
	public List<Property> getProperties() {
		// TODO Auto-generated method stub
		ArrayList<Property> properties = new ArrayList<Property>();
		return properties;
	}

	@Override
	protected int getBodyStartOctet() {
		// TODO Auto-generated method stub
		return 1;
	}

}
