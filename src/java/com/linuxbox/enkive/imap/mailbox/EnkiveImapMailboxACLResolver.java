package com.linuxbox.enkive.imap.mailbox;

import javax.mail.Flags;

import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.UnsupportedRightException;
import org.apache.james.mailbox.model.MailboxACL;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLEntryKey;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRight;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRights;
import org.apache.james.mailbox.model.SimpleMailboxACL;

public class EnkiveImapMailboxACLResolver implements MailboxACLResolver {

	@Override
	public MailboxACL applyGlobalACL(MailboxACL resourceACL,
			boolean resourceOwnerIsGroup) throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return SimpleMailboxACL.OWNER_FULL_ACL;
	}

	@Override
	public boolean hasRight(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACLRight right, MailboxACL resourceACL,
			String resourceOwner, boolean resourceOwnerIsGroup)
			throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isReadWrite(MailboxACLRights mailboxACLRights,
			Flags sharedFlags) throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public MailboxACLRights[] listRights(MailboxACLEntryKey key,
			GroupMembershipResolver groupMembershipResolver,
			String resourceOwner, boolean resourceOwnerIsGroup)
			throws UnsupportedRightException {
		// TODO Auto-generated method stub
		MailboxACLRights[] rights = new MailboxACLRights[1];
		rights[0] = SimpleMailboxACL.FULL_RIGHTS;
		return rights;
	}

	@Override
	public MailboxACLRights resolveRights(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACL resourceACL, String resourceOwner,
			boolean resourceOwnerIsGroup) throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return SimpleMailboxACL.FULL_RIGHTS;
	}

}
