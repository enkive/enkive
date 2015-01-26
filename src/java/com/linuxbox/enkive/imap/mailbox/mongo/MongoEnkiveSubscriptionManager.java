/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.SubscriptionException;

import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSession;
import com.linuxbox.enkive.imap.mailbox.EnkiveSubscriptionManager;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

public class MongoEnkiveSubscriptionManager extends EnkiveSubscriptionManager {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap.mailbox.mongo");

	/**
	 * Overridden method that returns all the mailboxes for a user.
	 */
	@Override
	public Collection<String> subscriptions(MailboxSession session)
			throws SubscriptionException {
		Collection<String> subscriptions = new HashSet<String>();

		Workspace workspace = ((EnkiveMailboxSession) session).getWorkspace();
		try {
			for (SearchQuery search : workspace.getSearches()) {
				if (search.isIMAP()) {
					subscriptions.add(search.getName());
				}
			}
		} catch (WorkspaceException e) {
			LOGGER.error("Error getting searches from user \""
					+ session.getUser().getUserName() + "\" workspace.", e);
			return new HashSet<String>();
		}

		return subscriptions;
	}
}
