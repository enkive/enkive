package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.acl.GroupMembershipResolver;

public class EnkiveImapGroupMembershipResolver implements
		GroupMembershipResolver {

	@Override
	public boolean isMember(String user, String group) {
		// XXX Groups are currently unsupported
		return false;
	}

}
