/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.permissions.message.MessagePermissionsService;

public class SpringContextPermissionService implements PermissionService {

	MessagePermissionsService messagePermissionService;

	@Override
	public String getCurrentUsername() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public boolean isAdmin() throws CannotGetPermissionsException {
		Collection<String> authorityStrings = getCurrentUserAuthorities();
		return authorityStrings.contains("ROLE_ENKIVE_ADMIN");
	}

	@Override
	public boolean canReadMessage(String userId, Message message)
			throws CannotGetPermissionsException {
		if (isAdmin())
			return true;

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
		if (isAdmin())
			return true;

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
		Collection<String> addresses = new HashSet<String>();
		addresses.add(userId);
		return addresses;
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
