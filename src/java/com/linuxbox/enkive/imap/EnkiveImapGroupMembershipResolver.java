package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.acl.GroupMembershipResolver;

public class EnkiveImapGroupMembershipResolver implements
		GroupMembershipResolver {

	@Override
	public boolean isMember(String user, String group) {
		// TODO Auto-generated method stub
		return true;
	}

}
