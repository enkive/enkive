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

import static com.linuxbox.enkive.authentication.EnkiveRoles.ROLE_ADMIN;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.linuxbox.enkive.authentication.ldap.EnkiveUserDetails;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.permissions.message.MessagePermissionsService;

public class SpringContextPermissionService implements PermissionService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.permissions");

	protected MessagePermissionsService messagePermissionService;

	@Override
	public String getCurrentUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public boolean isAdmin() throws CannotGetPermissionsException {
		Collection<String> authorityStrings = getCurrentUserAuthorities();
		final boolean result = authorityStrings.contains(ROLE_ADMIN);
		LOGGER.trace("user " + getCurrentUsername() + " determined to be "
				+ (result ? "ADMIN" : "not admin"));
		return result;
	}

	@Override
	public boolean canReadMessage(String userId, Message message)
			throws CannotGetPermissionsException {
		if (isAdmin()) {
			LOGGER.trace(userId + " determined to be administrator");
			return true;
		}
		LOGGER.trace(userId + " determined to not be administrator");

		Collection<String> canReadAddresses = canReadAddresses(userId);
		Collection<String> addressesInMessage = new HashSet<String>();
		addressesInMessage.addAll(message.getTo());
		addressesInMessage.addAll(message.getCc());
		addressesInMessage.addAll(message.getBcc());
		addressesInMessage.addAll(message.getFrom());
		addressesInMessage.add(message.getMailFrom());
		addressesInMessage.addAll(message.getRcptTo());

		return CollectionUtils
				.containsAny(addressesInMessage, canReadAddresses);
	}

	@Override
	public boolean canReadMessage(String userId, MessageSummary message)
			throws CannotGetPermissionsException {
		if (isAdmin()) {
			return true;
		}

		Collection<String> canReadAddresses = canReadAddresses(userId);
		Collection<String> addressesInMessage = new HashSet<String>();
		addressesInMessage.addAll(message.getTo());
		addressesInMessage.addAll(message.getCc());
		addressesInMessage.addAll(message.getBcc());
		addressesInMessage.addAll(message.getFrom());
		addressesInMessage.add(message.getMailFrom());
		addressesInMessage.addAll(message.getRcptTo());

		return CollectionUtils
				.containsAny(addressesInMessage, canReadAddresses);

	}

	@Override
	public Collection<String> canReadAddresses(String userId) {
		final UserDetails userDetails = (UserDetails) SecurityContextHolder
				.getContext().getAuthentication().getPrincipal();
		if (userDetails instanceof EnkiveUserDetails) {
			return ((EnkiveUserDetails) userDetails).getKnownEmailAddresses();
		} else {
			final Collection<String> addresses = new HashSet<String>();
			addresses.add(userId);
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
}
