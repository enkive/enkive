package com.linuxbox.enkive.authentication.ldap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

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

	protected Set<String> ldapEmailAddressAttributes;

	public EnkiveLdapUserDetailsContextMapper(String commaSeparatedList) {
		super();
		ldapEmailAddressAttributes = new HashSet<String>();
		LOGGER.fatal("constructor got " + commaSeparatedList + " for " + this);
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx,
			String username, Collection<GrantedAuthority> authority) {
		// FIXME :remove
		LOGGER.fatal("in customized mapUserFromContext");
		final UserDetails standardDetails = super.mapUserFromContext(ctx,
				username, authority);
		String[] attributeIds = { "mail" };
		try {
			Attributes attribs = ctx.getAttributes(username, attributeIds);
			NamingEnumeration<? extends Attribute> e = attribs.getAll();
			try {
				while (e.hasMore()) {
					Attribute o = e.next();
					LOGGER.fatal(o.getClass().getName() + " : " + o.toString());
				}
			} finally {
				e.close();
			}

			return standardDetails;
		} catch (NamingException e) {
			return null;
		}
		/*
		 * final EnkiveUserDetails enkiveDetails = new EnkiveUserDetails(
		 * standardDetails); return enkiveDetails;
		 */
	}

	@Override
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		throw new LdapUserDetailsException(
				"tried to save user details to an LDAP directory context, which is not supported by Enkive");
	}

	public void setLdapEmailAddressAttributes(String commaSeparatedList) {
		LOGGER.fatal("setLdapEmailAddressAttributes got " + commaSeparatedList
				+ " for " + this);
	}

	static class LdapUserDetailsException extends RuntimeException {
		private static final long serialVersionUID = -279076847305684214L;

		public LdapUserDetailsException(String message) {
			super(message);
		}
	}
}
