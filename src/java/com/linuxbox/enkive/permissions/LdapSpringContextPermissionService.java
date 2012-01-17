package com.linuxbox.enkive.permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.util.StringUtils;

public class LdapSpringContextPermissionService extends
		SpringContextPermissionService {

	protected String ldapEmailField;

	@Override
	public Collection<String> canReadAddresses(String userId) {
		Collection<String> addresses = new HashSet<String>();

		if (ldapEmailField != null && !ldapEmailField.isEmpty()) {
			addresses.addAll(getEmailAddressesFromDn(ldapEmailField));
		} else
			addresses.add(buildEmailAddressFromDc());

		return addresses;
	}

	protected String buildEmailAddressFromDc() {
		LdapUserDetails userDetails = (LdapUserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();

		String dn = userDetails.getDn();
		String[] dnList = StringUtils.delimitedListToStringArray(dn, ",");

		StringBuilder emailDomain = new StringBuilder();

		for (String dnComponent : dnList) {
			if (dnComponent.toLowerCase().startsWith("dc="))
				emailDomain.append(dnComponent.substring(3) + ".");
		}
		String email = getCurrentUsername() + "@"
				+ emailDomain.substring(0, emailDomain.length() - 1);

		return email;

	}

	protected Set<String> getEmailAddressesFromDn(String ldapEmailField) {
		Set<String> addresses = new HashSet<String>();
		LdapUserDetails userDetails = (LdapUserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String dn = userDetails.getDn();
		String[] dnList = StringUtils.delimitedListToStringArray(dn, ",");

		for (String dnComponent : dnList) {
			if (dnComponent.toLowerCase().startsWith(ldapEmailField + "="))
				addresses
						.add(dnComponent.substring(ldapEmailField.length() + 1));
		}
		return addresses;
	}

	public String getLdapEmailField() {
		return ldapEmailField;
	}

	public void setLdapEmailField(String ldapEmailField) {
		this.ldapEmailField = ldapEmailField; 
	}

}
