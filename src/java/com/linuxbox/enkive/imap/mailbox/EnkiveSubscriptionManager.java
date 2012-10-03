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

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.exception.SubscriptionException;

/**
 * Abstract implementation of SubscriptionManager
 * 
 * Since Enkive is designed as read-only, unimplemented methods fail silently
 * 
 * @author lee
 * 
 */
public abstract class EnkiveSubscriptionManager implements SubscriptionManager {

	@Override
	public void startProcessingRequest(MailboxSession session) {

	}

	@Override
	public void endProcessingRequest(MailboxSession session) {

	}

	@Override
	public void subscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {

	}

	@Override
	public void unsubscribe(MailboxSession session, String mailbox)
			throws SubscriptionException {

	}

}
