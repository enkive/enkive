package com.linuxbox.enkive.imap.authentication;

import org.apache.james.mailbox.store.Authenticator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Enkive implementation of the JAMES authenticator
 * 
 * @author lee
 * 
 */
public class EnkiveImapSpringContextAuthenticator implements Authenticator {

	protected AuthenticationManager authenticationManager;

	public EnkiveImapSpringContextAuthenticator(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public boolean isAuthentic(String userid, CharSequence passwd) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
				userid, passwd);
		Authentication authentication = authenticationManager
				.authenticate(token);
		return authentication.isAuthenticated();
	}

}
