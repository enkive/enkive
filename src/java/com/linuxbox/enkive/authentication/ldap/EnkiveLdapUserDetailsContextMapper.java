package com.linuxbox.enkive.authentication.ldap;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import com.linuxbox.enkive.authentication.EnkiveUserDetails;

public class EnkiveLdapUserDetailsContextMapper extends LdapUserDetailsMapper
		implements UserDetailsContextMapper {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication.ldap.EnkiveLdapUserDetailsContextMapper");

	protected String[] ldapEmailAddressAttributeIds;

	public EnkiveLdapUserDetailsContextMapper() {
		super();
	}

	public EnkiveLdapUserDetailsContextMapper(String commaSeparatedList) {
		super();
		setLdapEmailAddressAttributeIds(commaSeparatedList);
	}

	@Override
	public UserDetails mapUserFromContext(DirContextOperations ctx,
			String userName, Collection<? extends GrantedAuthority> authorities) {
		final UserDetails standardDetails = super.mapUserFromContext(ctx,
				userName, authorities);
		final EnkiveUserDetails enkiveDetails = new EnkiveUserDetails(
				standardDetails);

		for (String attributeId : ldapEmailAddressAttributeIds) {
			String[] emailAddresses = ctx.getStringAttributes(attributeId);
			if (null != emailAddresses) {
				for (String address : emailAddresses) {
					enkiveDetails.addKnownEmailAddress(address);
				}
			}
		}

		writeAuthenticationToLog(enkiveDetails);

		return enkiveDetails;
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
	public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
		throw new LdapUserDetailsException(
				"tried to save user details to an LDAP directory context, which is not supported by Enkive");
	}

	@Required
	public void setLdapEmailAddressAttributeIds(String commaSeparatedList) {
		ldapEmailAddressAttributeIds = commaSeparatedList.split(",");
	}

	static class LdapUserDetailsException extends RuntimeException {
		private static final long serialVersionUID = -279076847305684214L;

		public LdapUserDetailsException(String message) {
			super(message);
		}
	}
}
