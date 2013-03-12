/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
		return SimpleMailboxACL.OWNER_FULL_ACL;
	}

	@Override
	public boolean hasRight(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACLRight right, MailboxACL resourceACL,
			String resourceOwner, boolean resourceOwnerIsGroup)
			throws UnsupportedRightException {
		return true;
	}

	@Override
	public boolean isReadWrite(MailboxACLRights mailboxACLRights,
			Flags sharedFlags) throws UnsupportedRightException {
		return true;
	}

	@Override
	public MailboxACLRights[] listRights(MailboxACLEntryKey key,
			GroupMembershipResolver groupMembershipResolver,
			String resourceOwner, boolean resourceOwnerIsGroup)
			throws UnsupportedRightException {
		MailboxACLRights[] rights = new MailboxACLRights[1];
		rights[0] = SimpleMailboxACL.FULL_RIGHTS;
		return rights;
	}

	@Override
	public MailboxACLRights resolveRights(String requestUser,
			GroupMembershipResolver groupMembershipResolver,
			MailboxACL resourceACL, String resourceOwner,
			boolean resourceOwnerIsGroup) throws UnsupportedRightException {
		return SimpleMailboxACL.FULL_RIGHTS;
	}

}
