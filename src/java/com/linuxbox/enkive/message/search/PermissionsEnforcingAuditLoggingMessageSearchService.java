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
 ******************************************************************************/
/*
 * 
 */
package com.linuxbox.enkive.message.search;

import java.util.Collection;
import java.util.HashMap;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.SearchResult;

import static com.linuxbox.enkive.search.Constants.*;

public class PermissionsEnforcingAuditLoggingMessageSearchService extends
		AuditLoggingMessageSearchService {

	PermissionService permService;

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {
		try {
			if (!permService.isAdmin()) {
				Collection<String> addresses = permService
						.canReadAddresses(permService.getCurrentUsername());
				if (addresses.isEmpty()) {
					// If there are no permissions to read any addresses, void
					// the query
					fields.clear();
				} else {
					StringBuilder addressesString = new StringBuilder();
					for (String address : addresses) {
						addressesString.append(address);
						addressesString.append("; ");
					}

					fields.put(PERMISSIONS_SENDER_PARAMETER,
							addressesString.toString());
					fields.put(PERMISSIONS_RECIPIENT_PARAMETER,
							addressesString.toString());
				}
			}
		} catch (CannotGetPermissionsException e) {
			throw new MessageSearchException(
					"Could not get permissions for current user", e);
		}
		return super.search(fields);

	}

	public PermissionService getPermService() {
		return permService;
	}

	public void setPermService(PermissionService permService) {
		this.permService = permService;
	}

}
