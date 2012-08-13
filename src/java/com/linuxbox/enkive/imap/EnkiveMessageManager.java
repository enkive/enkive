package com.linuxbox.enkive.imap;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Flags;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.UnsupportedRightException;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.MessageResultIterator;
import org.apache.james.mailbox.model.SearchQuery;
import org.apache.james.mailbox.model.MailboxACL.EditMode;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLEntryKey;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRight;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRights;
import org.apache.james.mailbox.model.MessageResult.FetchGroup;

public class EnkiveMessageManager implements MessageManager {

	@Override
	public long getMessageCount(MailboxSession mailboxSession)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean isWriteable(MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isModSeqPermanent(MailboxSession session) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Long> search(SearchQuery searchQuery,
			MailboxSession mailboxSession) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Long> expunge(MessageRange set,
			MailboxSession mailboxSession) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, Flags> setFlags(Flags flags, boolean value,
			boolean replace, MessageRange set, MailboxSession mailboxSession)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long appendMessage(InputStream msgIn, Date internalDate,
			MailboxSession mailboxSession, boolean isRecent, Flags flags)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MessageResultIterator getMessages(MessageRange set,
			FetchGroup fetchGroup, MailboxSession mailboxSession)
			throws MailboxException {
		return new EnkiveMessageResultIterator();
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasRight(MailboxACLRight right, MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MailboxACLRights myRights(MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MailboxACLRights[] listRigths(MailboxACLEntryKey identifier,
			MailboxSession session) throws UnsupportedRightException {
		MailboxACLRights[] rights = new MailboxACLRights[0];
		// TODO Auto-generated method stub
		return rights;
	}

	@Override
	public void setRights(MailboxACLEntryKey identifier, EditMode editMode,
			MailboxACLRights mailboxAclRights) throws UnsupportedRightException {
		// TODO Auto-generated method stub

	}

	@Override
	public MetaData getMetaData(
			boolean resetRecent,
			MailboxSession mailboxSession,
			org.apache.james.mailbox.MessageManager.MetaData.FetchGroup fetchGroup)
			throws MailboxException {
		return new EnkiveMessageManagerMetaData();
		// TODO Auto-generated method stub
	}

}
