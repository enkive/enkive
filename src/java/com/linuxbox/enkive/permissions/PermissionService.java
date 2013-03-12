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

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;

public interface PermissionService {

	public String getCurrentUsername();

	public boolean isAdmin() throws CannotGetPermissionsException;

	public Collection<String> getCurrentUserAuthorities()
			throws CannotGetPermissionsException;

	public boolean canReadMessage(String userId, Message message)
			throws CannotGetPermissionsException;

	public boolean canReadMessage(String userId, MessageSummary message)
			throws CannotGetPermissionsException;

	public boolean canReadAttachment(String userId, String attachmentId)
			throws CannotGetPermissionsException;

	public Collection<String> canReadAddresses(String userId);

}
