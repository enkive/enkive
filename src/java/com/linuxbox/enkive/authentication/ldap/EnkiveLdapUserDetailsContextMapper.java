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
import com.linuxbox.enkive.normalization.EmailAddressNormalizer;

public class EnkiveLdapUserDetailsContextMapper extends LdapUserDetailsMapper
		implements UserDetailsContextMapper {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication.ldap.EnkiveLdapUserDetailsContextMapper");

	protected EmailAddressNormalizer emailAddressNormalizer;
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
				standardDetails, emailAddressNormalizer);

		for (String attributeId : ldapEmailAddressAttributeIds) {
			String[] emailAddresses = ctx.getStringAttributes(attributeId);
			if (null != emailAddresses) {
				for (String address : emailAddresses) {
					enkiveDetails.addKnownEmailAddress(address);
				}
			}
		}

		if (enkiveDetails.getKnownEmailAddresses().isEmpty()
				&& !enkiveDetails.isEnkiveAdmin()) {
			LOGGER.warn("User \""
					+ userName
					+ "\" logged in as non-admin and without any known email addresses, so this user will not be able to conduct any searches as it's impossible to constrain the results.");
		}

		enkiveDetails.writeAuthenticationToLog();

		return enkiveDetails;
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

	@Required
	public void setEmailAddressNormalizer(
			EmailAddressNormalizer emailAddressNormalizer) {
		this.emailAddressNormalizer = emailAddressNormalizer;
	}

	static class LdapUserDetailsException extends RuntimeException {
		private static final long serialVersionUID = -279076847305684214L;

		public LdapUserDetailsException(String message) {
			super(message);
		}
	}
}
