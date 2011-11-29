package com.linuxbox.enkive.permissions;

import java.util.Collection;
import java.util.HashSet;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.util.StringUtils;

public class LdapSpringContextPermissionService extends
		SpringContextPermissionService {

	@Override
	public Collection<String> canReadAddresses(String userId) {
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

		Collection<String> addresses = new HashSet<String>();
		addresses.add(email);
		return addresses;
	}

}
