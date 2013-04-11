package com.linuxbox.enkive.authentication;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import com.linuxbox.enkive.authentication.EnkiveUserDetails;

public class EnkivePropFileUserDetailsContextMapper
		implements UserDetailsService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication.ldap.EnkiveLdapUserDetailsContextMapper");

	protected String defaultDomain;

	public EnkivePropFileUserDetailsContextMapper() {
		super();
	}

	/**
	 * Write an entry to the text log indicating that a given user has logged in
	 * via LDAP. This is broken out to a separate method given its length.
	 * 
	 * @param enkiveDetails
	 */
	protected void writeAuthenticationToLog(EnkiveUserDetails enkiveDetails) {
		if (!LOGGER.isInfoEnabled()) {
			return;
		}

		StringBuffer info = new StringBuffer();
		info.append(enkiveDetails.getUsername()).append(
				" authenticated via LDAP with role(s) {");

		boolean first = true;
		for (GrantedAuthority ga : enkiveDetails.getAuthorities()) {
			if (first) {
				first = false;
			} else {
				info.append(", ");
			}
			info.append(ga.getAuthority());
		}

		info.append("} and email address(es) {");

		first = true;
		for (String email : enkiveDetails.getKnownEmailAddresses()) {
			if (first) {
				first = false;
			} else {
				info.append(", ");
			}
			info.append(email);
		}

		info.append("}.");

		LOGGER.info(info);
	}


	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
