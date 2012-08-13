package com.linuxbox.enkive.imap;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;

import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.model.MailboxACL;

public class EnkiveMessageManagerMetaData implements MessageManager.MetaData {

	@Override
	public List<Long> getRecent() {
		// TODO Auto-generated method stub
		ArrayList<Long> uids = new ArrayList<Long>();
		uids.add((long) 1);
		return uids;
	}

	@Override
	public long countRecent() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Flags getPermanentFlags() {
		return new Flags();
		// TODO Auto-generated method stub
	}

	@Override
	public long getUidValidity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getUidNext() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getHighestModSeq() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getMessageCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public long getUnseenCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Long getFirstUnseen() {
		// TODO Auto-generated method stub
		return (long) 1;
	}

	@Override
	public boolean isWriteable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModSeqPermanent() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MailboxACL getACL() {
		// TODO Auto-generated method stub
		return null;
	}

}
