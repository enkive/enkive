package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.store.Authenticator;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;

public class EnkiveMailboxManager extends StoreMailboxManager<Long> {

	public EnkiveMailboxManager(
			MailboxSessionMapperFactory<Long> mailboxSessionMapperFactory,
			Authenticator authenticator, MailboxACLResolver aclResolver,
			GroupMembershipResolver groupMembershipResolver) {
		super(mailboxSessionMapperFactory, authenticator, aclResolver,
				groupMembershipResolver);
		// TODO Auto-generated constructor stub
	}

}
