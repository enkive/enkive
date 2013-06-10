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
package com.linuxbox.enkive.imap.authentication;

import org.springframework.security.core.context.SecurityContextHolder;

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
		// put authenticator in thread-local storage (so it can be stashed in the user's MailboxSession (and cleared)
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return authentication.isAuthenticated();
	}

}
