package com.linuxbox.enkive.authentication.ldap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class EnkiveUserDetails extends User {
	private static final long serialVersionUID = 3003366042873560086L;

	Set<String> knownEmailAddresses;

	public EnkiveUserDetails(String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked, authorities);

		knownEmailAddresses = new HashSet<String>();
	}

	public EnkiveUserDetails(UserDetails other) {
		this(other.getUsername(), other.getPassword(), other.isEnabled(), other
				.isAccountNonExpired(), other.isCredentialsNonExpired(), other
				.isAccountNonLocked(), other.getAuthorities());
		if (other instanceof EnkiveUserDetails) {
			setKnownEmailAddresses(((EnkiveUserDetails) other)
					.getKnownEmailAddresses());
		}
	}

	public void setKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.clear();
		knownEmailAddresses.addAll(addresses);
	}

	public void addKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.addAll(addresses);
	}

	public Set<String> getKnownEmailAddresses() {
		return Collections.unmodifiableSet(knownEmailAddresses);
	}
}
