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

					fields.put(PERMISSIONS_SENDER_PARAMETER, addressesString.toString());
					fields.put(PERMISSIONS_RECIPIENT_PARAMETER, addressesString.toString());
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
