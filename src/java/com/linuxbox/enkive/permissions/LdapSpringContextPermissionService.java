/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
package com.linuxbox.enkive.permissions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.util.StringUtils;

// FIXME: remove extra tracing output before release

public class LdapSpringContextPermissionService extends
		SpringContextPermissionService {
	protected String ldapEmailAttributes;

	@Override
	public Collection<String> canReadAddresses(String userId) {
		Collection<String> addresses;

		// if a specific field for email addresses is not listed
		// attempt to build it from the dn
		if (ldapEmailAttributes != null && !ldapEmailAttributes.isEmpty()) {
			addresses = getEmailAddressesFromDn(ldapEmailAttributes);
		} else {
			addresses = new HashSet<String>();
			addresses.add(buildEmailAddressFromDc());
		}

		return addresses;
	}

	protected String buildEmailAddressFromDc() {
		LdapUserDetails userDetails = (LdapUserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();

		String dn = userDetails.getDn();
		String[] dnList = StringUtils.delimitedListToStringArray(dn, ",");

		StringBuilder emailDomain = new StringBuilder();

		for (String dnComponent : dnList) {
			if (dnComponent.toLowerCase().startsWith("dc=")) {
				emailDomain.append(dnComponent.substring(3) + ".");
			}
		}
		String email = getCurrentUsername() + "@"
				+ emailDomain.substring(0, emailDomain.length() - 1);

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("buildEmailAddressFromDc returning \"" + email + "\".");
		}

		return email;
	}

	protected Set<String> getEmailAddressesFromDn(String ldapEmailField) {
		LOGGER.trace("getEmailAddressesFromDn entering with ldapEmailField equal to \""
				+ ldapEmailField + "\".");

		Set<String> addresses = new HashSet<String>();
		LdapUserDetails userDetails = (LdapUserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		String dn = userDetails.getDn();
		LOGGER.trace("getEmailAddressesFromDn retrieved LDAP user details of \""
				+ dn + "\".");

		String[] dnList = StringUtils.delimitedListToStringArray(dn, ",");

		for (String dnComponent : dnList) {
			LOGGER.trace("getEmailAddressesFromDn considering \"" + dnComponent
					+ "\".");

			if (dnComponent.toLowerCase().startsWith(ldapEmailField + "=")) {
				final String value = dnComponent.substring(ldapEmailField
						.length() + 1);
				addresses.add(value);
				LOGGER.trace("getEmailAddressesFromDn adding \"" + value
						+ "\".");
			}
		}

		LOGGER.trace("getEmailAddressesFromDn exiting with list of "
				+ addresses.size() + " email addresses.");

		return addresses;
	}

	public String getLdapEmailAttributes() {
		return ldapEmailAttributes;
	}

	public void setLdapEmailAttributes(String ldapEmailAttributes) {
		LOGGER.trace("initializing LdapSpringContextPermissionService's LDAP email field to \""
				+ ldapEmailAttributes + "\".");

		this.ldapEmailAttributes = ldapEmailAttributes;
	}
}
