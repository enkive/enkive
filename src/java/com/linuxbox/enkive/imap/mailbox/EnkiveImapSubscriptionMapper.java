/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.imap.mailbox;

import java.util.ArrayList;
import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.apache.james.mailbox.store.user.SubscriptionMapper;
import org.apache.james.mailbox.store.user.model.Subscription;

/**
 * Implementation of SubscriptionMapper Since Enkive IMAP access is designed as
 * read-only, unimplemented methods are designed to fail silently
 * 
 * @author lee
 * 
 */
public class EnkiveImapSubscriptionMapper implements SubscriptionMapper {

	MailboxSession session;

	public EnkiveImapSubscriptionMapper(MailboxSession session) {
		this.session = session;
	}

	@Override
	public void endRequest() {

	}

	@Override
	public <T> T execute(Transaction<T> transaction) throws MailboxException {
		throw new MailboxException("Subscriptions are not supported");
	}

	@Override
	public Subscription findMailboxSubscriptionForUser(String user,
			String mailbox) throws SubscriptionException {
		throw new SubscriptionException();
	}

	@Override
	public void save(Subscription subscription) throws SubscriptionException {

	}

	@Override
	public List<Subscription> findSubscriptionsForUser(String user)
			throws SubscriptionException {
		// Return empty list, since this action is unsupported
		return new ArrayList<Subscription>();
	}

	@Override
	public void delete(Subscription subscription) throws SubscriptionException {

	}

}
