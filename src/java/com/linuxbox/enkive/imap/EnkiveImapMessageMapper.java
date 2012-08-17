package com.linuxbox.enkive.imap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageMetaData;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.store.SimpleMessageMetaData;
import org.apache.james.mailbox.store.mail.AbstractMessageMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class EnkiveImapMessageMapper extends AbstractMessageMapper<Long> {

	MessageRetrieverService retrieverService;

	public EnkiveImapMessageMapper(MailboxSession mailboxSession,
			EnkiveImapStore store, MessageRetrieverService retrieverService) {
		super(mailboxSession, store, store);
		this.retrieverService = retrieverService;
	}

	@Override
	public Iterator<Message<Long>> findInMailbox(Mailbox<Long> mailbox,
			MessageRange set,
			org.apache.james.mailbox.store.mail.MessageMapper.FetchType type,
			int limit) throws MailboxException {
		ArrayList<Message<Long>> messages = new ArrayList<Message<Long>>();
		// TODO Auto-generated method stub
		if (set.getUidFrom() < 1 || set.getType().toString().equals("ONE")) {
			try {
				com.linuxbox.enkive.message.Message message = retrieverService
						.retrieve("e826e2fb14162842d9caf023163338d20e2820bb");
				messages.add(new EnkiveImapMessage(message));
				System.out.println("HERE");
			} catch (CannotRetrieveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(messages.size() + " " + set.getUidFrom() + " "
				+ set.getUidTo() + " " + set.getType().toString());
		return messages.iterator();
	}

	@Override
	public Map<Long, MessageMetaData> expungeMarkedForDeletionInMailbox(
			Mailbox<Long> mailbox, MessageRange set) throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long countMessagesInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public long countUnseenMessagesInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(Mailbox<Long> mailbox, Message<Long> message)
			throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	public Long findFirstUnseenMessageUid(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		return (long) 0;
	}

	@Override
	public List<Long> findRecentMessageUidsInMailbox(Mailbox<Long> mailbox)
			throws MailboxException {
		// TODO Auto-generated method stub
		ArrayList<Long> messageIds = new ArrayList<Long>();
		return messageIds;
	}

	@Override
	public void endRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	protected MessageMetaData save(Mailbox<Long> mailbox, Message<Long> message)
			throws MailboxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected MessageMetaData copy(Mailbox<Long> mailbox, long uid,
			long modSeq, Message<Long> original) throws MailboxException {
		// TODO Auto-generated method stub
		return new SimpleMessageMetaData(original);
	}

	@Override
	protected void begin() throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void commit() throws MailboxException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void rollback() throws MailboxException {
		// TODO Auto-generated method stub

	}

}
