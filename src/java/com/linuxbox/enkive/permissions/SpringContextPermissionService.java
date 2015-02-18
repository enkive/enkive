/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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

import static com.linuxbox.enkive.authentication.EnkiveRoles.ROLE_ADMIN;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.linuxbox.enkive.authentication.EnkiveUserDetails;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.normalization.EmailAddressNormalizer;
import com.linuxbox.enkive.permissions.message.MessagePermissionsService;

public class SpringContextPermissionService implements PermissionService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.permissions");

	protected MessagePermissionsService messagePermissionService;
	protected EmailAddressNormalizer emailAddressNormalizer;

	@Override
	public String getCurrentUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	/**
	 * If the security principal is an EnkiveUserDetails, can test directly if
	 * user is an enkive admin. Otherwise we have to search through the granted
	 * authorities one by one.
	 */
	public boolean isAdmin() throws CannotGetPermissionsException {
		final Authentication authentication = SecurityContextHolder
				.getContext().getAuthentication();

		final Object detailsObj = authentication.getPrincipal();
		if (detailsObj instanceof EnkiveUserDetails) {
			final boolean isAdmin = ((EnkiveUserDetails) detailsObj)
					.isEnkiveAdmin();
			return isAdmin;
		}

		for (GrantedAuthority a : authentication.getAuthorities()) {
			if (a.getAuthority().equals(ROLE_ADMIN)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean canReadMessage(String userId, MessageSummary message)
			throws CannotGetPermissionsException {
		if (isAdmin()) {
			return true;
		}

		Collection<String> canReadAddresses = canReadAddresses(userId);

		HashSet<String> originalMessageAddresses = new HashSet<String>();
		originalMessageAddresses.addAll(message.getTo());
		originalMessageAddresses.addAll(message.getCc());
		originalMessageAddresses.addAll(message.getBcc());
		originalMessageAddresses.addAll(message.getFrom());
		originalMessageAddresses.add(message.getMailFrom());
		originalMessageAddresses.addAll(message.getRcptTo());

		HashSet<String> normalizedMessageAddresses = new HashSet<String>(
				originalMessageAddresses.size());
		com.linuxbox.util.CollectionUtils.addAllMapped(
				normalizedMessageAddresses, originalMessageAddresses,
				emailAddressNormalizer);

		return CollectionUtils.containsAny(normalizedMessageAddresses,
				canReadAddresses);
	}

	@Override
	public Collection<String> canReadAddresses(String userId) {
		
		SecurityContext ctx = SecurityContextHolder.getContext();
		Authentication auth = ctx.getAuthentication();
		
		final UserDetails userDetails = (UserDetails) auth.getPrincipal();
		
		if (userDetails instanceof EnkiveUserDetails) {
			return ((EnkiveUserDetails) userDetails)
					.getKnownNormalizedEmailAddresses();
		} else {
			LOGGER.warn("user \""
					+ userId
					+ "\" did not seem to authenticate producing an instance of EnkiveUserDetails");

			// this is assuming that the userId can be treated as an email
			// address; ideally we'll never take this path; perhaps we should
			// throw an exception
			final Collection<String> addresses = new HashSet<String>();
			addresses.add(emailAddressNormalizer.map(userId));

			return addresses;
		}
	}

	@Override
	public Collection<String> getCurrentUserAuthorities()
			throws CannotGetPermissionsException {
		Collection<String> authorityStrings = new HashSet<String>();
		for (GrantedAuthority auth : SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities()) {
			authorityStrings.add(auth.getAuthority());
		}
		return authorityStrings;
	}

	public MessagePermissionsService getMessagePermissionService() {
		return messagePermissionService;
	}

	@Required
	public void setMessagePermissionService(
			MessagePermissionsService messagePermissionService) {
		this.messagePermissionService = messagePermissionService;
	}

	@Override
	public boolean canReadAttachment(String userId, String attachmentId)
			throws CannotGetPermissionsException {
		if (isAdmin())
			return true;
		else
			return messagePermissionService.canReadAttachment(
					canReadAddresses(userId), attachmentId);
	}

	// Should this be shifted up to the PermissionService interface?
	@Required
	public void setEmailAddressNormalizer(
			EmailAddressNormalizer emailAddressNormalizer) {
		this.emailAddressNormalizer = emailAddressNormalizer;
	}
}
