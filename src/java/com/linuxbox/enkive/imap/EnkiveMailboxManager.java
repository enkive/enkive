package com.linuxbox.enkive.imap;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.MailboxListener;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.exception.BadCredentialsException;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxMetaData;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MailboxQuery;
import org.apache.james.mailbox.model.MessageRange;
import org.slf4j.Logger;

public class EnkiveMailboxManager implements MailboxManager {

	@Override
	public void startProcessingRequest(MailboxSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void endProcessingRequest(MailboxSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(MailboxPath mailboxPath, MailboxListener listener,
			MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(MailboxPath mailboxPath,
			MailboxListener listner, MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addGlobalListener(MailboxListener listener,
			MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeGlobalListener(MailboxListener listner,
			MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public char getDelimiter() {
		// TODO Auto-generated method stub
		return '.';
	}

	@Override
	public MessageManager getMailbox(MailboxPath mailboxPath,
			MailboxSession session) throws MailboxException {
		MessageManager mm = new EnkiveMessageManager();
		// TODO Auto-generated method stub
		return mm;
	}

	@Override
	public void createMailbox(MailboxPath mailboxPath,
			MailboxSession mailboxSession) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteMailbox(MailboxPath mailboxPath, MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameMailbox(MailboxPath from, MailboxPath to,
			MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<MessageRange> copyMessages(MessageRange set, MailboxPath from,
			MailboxPath to, MailboxSession session) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<MailboxMetaData> search(MailboxQuery expression,
			MailboxSession session) throws MailboxException {
		List<MailboxMetaData> metaDataList = new ArrayList<MailboxMetaData>();
		MailboxMetaData md = new EnkiveMailboxMetaData(
				MailboxPath.inbox(session));
		metaDataList.add(md);
		// TODO Auto-generated method stub
		return metaDataList;
	}

	@Override
	public boolean mailboxExists(MailboxPath mailboxPath, MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public MailboxSession createSystemSession(String userName, Logger log)
			throws BadCredentialsException, MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MailboxSession login(String userid, String passwd, Logger log)
			throws BadCredentialsException, MailboxException {
		MailboxSession session = new EnkiveMailboxSession();
		// TODO Auto-generated method stub
		return session;
	}

	@Override
	public void logout(MailboxSession session, boolean force)
			throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<MailboxPath> list(MailboxSession session)
			throws MailboxException {
		// TODO Auto-generated method stub
		ArrayList<MailboxPath> mailboxPaths = new ArrayList<MailboxPath>();

		mailboxPaths.add(MailboxPath.inbox(session));
		return mailboxPaths;
	}

}
