package com.linuxbox.enkive.authentication.ldap;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class EnkiveLdapUserDetailsContextMapper extends LdapUserDetailsMapper
		implements UserDetailsContextMapper {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication.ldap.EnkiveLdapUserDetailsContextMapper");

	protected String[] ldapEmailAddressAttributes;

	public EnkiveLdapUserDetailsContextMapper(String commaSeparatedList) {
		super();
		LOGGER.fatal("constructor got " + commaSeparatedList + " for " + this);
		setLdapEmailAddressAttributes(commaSeparatedList);
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx,
			String userName, Collection<GrantedAuthority> authorities) {
		final UserDetails standardDetails = super.mapUserFromContext(ctx,
				userName, authorities);
		final EnkiveUserDetails enkiveDetails = new EnkiveUserDetails(
				standardDetails);

		// FIXME: this is only here due to an apparent Spring bug.
		String[] attributeIds = { "mail" };
		for (String attributeId : attributeIds) {
			String[] emailAddresses = ctx.getStringAttributes(attributeId);
			for (String address : emailAddresses) {
				enkiveDetails.addKnownEmailAddress(address);
			}
		}

		return enkiveDetails;
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		throw new LdapUserDetailsException(
				"tried to save user details to an LDAP directory context, which is not supported by Enkive");
	}

	public void setLdapEmailAddressAttributes(String commaSeparatedList) {
		LOGGER.fatal("setLdapEmailAddressAttributes got " + commaSeparatedList
				+ " for " + this);
		ldapEmailAddressAttributes = commaSeparatedList.split(",");
	}

	static class LdapUserDetailsException extends RuntimeException {
		private static final long serialVersionUID = -279076847305684214L;

		public LdapUserDetailsException(String message) {
			super(message);
		}
	}
}
