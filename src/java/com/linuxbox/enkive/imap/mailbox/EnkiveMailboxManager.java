package com.linuxbox.enkive.imap.mailbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.Authenticator;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;

public class EnkiveMailboxManager extends StoreMailboxManager<String> {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public EnkiveMailboxManager(
			MailboxSessionMapperFactory<String> mailboxSessionMapperFactory,
			Authenticator authenticator, MailboxACLResolver aclResolver,
			GroupMembershipResolver groupMembershipResolver) {
		super(mailboxSessionMapperFactory, authenticator, aclResolver,
				groupMembershipResolver);
	}

	// Convenience method for use with spring
	public void startup() {
		try {
			init();
		} catch (MailboxException e) {
			LOGGER.warn("Could not initialize Enkive mailbox manager", e);
		}
	}

}
