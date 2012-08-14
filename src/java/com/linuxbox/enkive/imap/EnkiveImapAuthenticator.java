package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.store.Authenticator;

public class EnkiveImapAuthenticator implements Authenticator {

	@Override
	public boolean isAuthentic(String userid, CharSequence passwd) {
		// TODO Auto-generated method stub
		return true;
	}

}
