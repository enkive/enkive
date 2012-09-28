package com.linuxbox.enkive.imap.authentication;

import org.apache.james.mailbox.store.Authenticator;

import com.linuxbox.enkive.imap.EnkiveImapAccountCreator;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public class EnkiveImapAccountCreatingAuthenticator implements Authenticator {

	Authenticator authenticator;
	EnkiveImapAccountCreator accountCreator;
	
	public EnkiveImapAccountCreatingAuthenticator(Authenticator authenticator,
			EnkiveImapAccountCreator accountCreator) {
		this.authenticator = authenticator;
		this.accountCreator = accountCreator;

	}

	@Override
	public boolean isAuthentic(String userid, CharSequence passwd) {
		boolean authentic = authenticator.isAuthentic(userid, passwd);
		if(authentic)
			if(!accountCreator.accountExists(userid))
				try {
					accountCreator.createImapAccount(userid);
				} catch (MessageSearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		return authentic;
	}

}
