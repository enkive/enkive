package com.linuxbox.enkive.imap;

import javax.mail.Flags;

import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.UnsupportedRightException;
import org.apache.james.mailbox.model.MailboxACL;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLEntryKey;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRight;
import org.apache.james.mailbox.model.MailboxACL.MailboxACLRights;

public class EnkiveImapMailboxACLResolver implements MailboxACLResolver {

	@Override
	public MailboxACL applyGlobalACL(MailboxACL resourceACL,
			boolean resourceOwnerIsGroup) throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRight(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACLRight right, MailboxACL resourceACL,
			String resourceOwner, boolean resourceOwnerIsGroup)
			throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return false;
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
		return null;
	}

	@Override
	public MailboxACLRights resolveRights(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACL resourceACL, String resourceOwner,
			boolean resourceOwnerIsGroup) throws UnsupportedRightException {
		// TODO Auto-generated method stub
		return null;
	}

}
