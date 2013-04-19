package com.linuxbox.enkive.authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.linuxbox.enkive.normalization.EmailAddressNormalizer;

/**
 * Puts an Enkive User Details facade over other User Details, to which most
 * methods are delegated to. Enkive User Details adds a set of known email
 * addresses and the methods to manage that set.
 */
public class EnkiveUserDetails implements UserDetails {
	private static final long serialVersionUID = 3003366042873560086L;
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.authentication");

	protected final UserDetails delegate;
	protected final EmailAddressNormalizer emailAddressNormalizer;

	/**
	 * Email addresses that this user is known by and therefore has access to
	 * emails sent from or received by.
	 */
	protected Set<String> knownEmailAddresses;

	protected Set<String> knownNormalizedEmailAddresses;
	protected boolean isEnkiveAdmin = false;
	protected boolean isEnkiveUser = false;

	public EnkiveUserDetails(UserDetails plainUser,
			EmailAddressNormalizer emailAddressNormalizer) {
		this.delegate = plainUser;
		this.emailAddressNormalizer = emailAddressNormalizer;

		knownEmailAddresses = new HashSet<String>();

		if (plainUser instanceof EnkiveUserDetails) {
			knownEmailAddresses
					.addAll(((EnkiveUserDetails) plainUser).knownEmailAddresses);
		}

		determineRoles(plainUser);
	}

	protected void determineRoles(UserDetails user) {
		for (GrantedAuthority authority : user.getAuthorities()) {
			final String authorityString = authority.getAuthority();

			if (authorityString.equals(EnkiveRoles.ROLE_ADMIN)) {
				isEnkiveAdmin = true;
			} else if (authorityString.equals(EnkiveRoles.ROLE_USER)) {
				isEnkiveUser = true;
			}
		}
	}

	public void setKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.clear();
		knownEmailAddresses.addAll(addresses);
		knownNormalizedEmailAddresses = null; // nullify so will be regenerated
	}

	public void addKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.addAll(addresses);
		knownNormalizedEmailAddresses = null; // nullify so will be regenerated
	}

	public void addKnownEmailAddress(String address) {
		knownEmailAddresses.add(address);
		knownNormalizedEmailAddresses = null; // nullify so will be regenerated
	}

	public Set<String> getKnownEmailAddresses() {
		return Collections.unmodifiableSet(knownEmailAddresses);
	}

	public Set<String> getKnownNormalizedEmailAddresses() {
		if (null == knownNormalizedEmailAddresses) {
			knownNormalizedEmailAddresses = new HashSet<String>();
			for (String address : knownEmailAddresses) {
				final String normalizedAddress = emailAddressNormalizer
						.normalize(address);
				knownNormalizedEmailAddresses.add(normalizedAddress);
			}
		}

		return knownNormalizedEmailAddresses;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return delegate.getAuthorities();
	}

	@Override
	public String getPassword() {
		return delegate.getPassword();
	}

	@Override
	public String getUsername() {
		return delegate.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return delegate.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return delegate.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return delegate.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return delegate.isEnabled();
	}

	public void writeAuthenticationToLog() {
		if (!LOGGER.isInfoEnabled()) {
			return;
		}

		StringBuffer info = new StringBuffer();
		info.append(getUsername()).append(
				" authenticated via LDAP with role(s) {");

		boolean first = true;
		for (GrantedAuthority ga : getAuthorities()) {
			if (first) {
				first = false;
			} else {
				info.append(", ");
			}
			info.append(ga.getAuthority());
		}

		info.append("} and email address(es) {");

		first = true;
		for (String email : getKnownEmailAddresses()) {
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

	public boolean isEnkiveAdmin() {
		return isEnkiveAdmin;
	}

	public boolean isEnkiveUser() {
		return isEnkiveUser;
	}
}
