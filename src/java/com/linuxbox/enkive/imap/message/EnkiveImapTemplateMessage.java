package com.linuxbox.enkive.imap.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;

public class EnkiveImapTemplateMessage extends AbstractMessage<String> {

	@Override
	public Date getInternalDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailboxId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getUid() {
		// TODO Auto-generated method stub
		return 0;
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
		return false;
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
		return null;
	}

	@Override
	public String getMediaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getFullContentOctets() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getTextualLineCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Property> getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getBodyStartOctet() {
		// TODO Auto-generated method stub
		return 0;
	}

}
