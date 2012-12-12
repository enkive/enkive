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
/*
 * 
 */
package com.linuxbox.enkive.message.search;

import static com.linuxbox.enkive.search.Constants.PERMISSIONS_RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.PERMISSIONS_SENDER_PARAMETER;

import java.util.Collection;
import java.util.HashMap;

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public class PermissionsEnforcingAuditLoggingMessageSearchService extends
		AuditLoggingMessageSearchService {

	PermissionService permService;

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {
		try {
			if (permService.isAdmin()) {
				LOGGER.trace("message search performed by administrator");
			} else {
				LOGGER.trace("message search performed by non-admininstrator");

				Collection<String> addresses = permService
						.canReadAddresses(permService.getCurrentUsername());
				if (addresses.isEmpty()) {
					// If there are no permissions to read any addresses, void
					// the query
					LOGGER.warn("search performed by non-admin could not find any email addresses to limit search.");
					/* TODO: Is this correct behavior -- clearing search fields? */
					fields.clear();
				} else {
					StringBuilder addressesStringBuilder = new StringBuilder();
					for (String address : addresses) {
						addressesStringBuilder.append(address);
						addressesStringBuilder.append("; ");
					}

					final String addressesString = addressesStringBuilder
							.toString();

					LOGGER.trace("search performed by non-admin using email addresses to limit search: "
							+ addressesString);

					fields.put(PERMISSIONS_SENDER_PARAMETER, addressesString);
					fields.put(PERMISSIONS_RECIPIENT_PARAMETER, addressesString);
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
