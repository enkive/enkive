/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.imap.mongo;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.model.Mailbox;

import com.linuxbox.enkive.imap.EnkiveImapStore;
import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSession;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;

public class MongoEnkiveImapStore extends EnkiveImapStore {

	SearchQueryBuilder searchQueryBuilder;

	public MongoEnkiveImapStore(SearchQueryBuilder searchQueryBuilder) {
		this.searchQueryBuilder = searchQueryBuilder;
	}

	@Override
	public long lastUid(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		SearchQuery query = null;
		try {
			EnkiveMailboxSession enkiveSession = (EnkiveMailboxSession) session;
			query = searchQueryBuilder.getSearchQueryByWorkspaceNameImap(
					enkiveSession.getWorkspace(), mailbox.getName(), true);
		} catch (WorkspaceException e) {
			throw new MailboxException("Could not find query for mailbox "
					+ mailbox.getName(), e);
		} catch (ClassCastException e) {
			throw new MailboxException(
					"Unexpected failed class cast from MailboxSession to EnkiveMailboxSession",
					e);
		}

		long uid = 1;
		if (query != null) {
			uid = query.getResult().getNextUID() - 1;
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("lastUid " + uid);
		return uid;
	}

}
