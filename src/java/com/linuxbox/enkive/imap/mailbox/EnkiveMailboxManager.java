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
package com.linuxbox.enkive.imap.mailbox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.Authenticator;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;

public class EnkiveMailboxManager extends StoreMailboxManager<String> {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");

	public EnkiveMailboxManager(
			MailboxSessionMapperFactory<String> mailboxSessionMapperFactory,
			Authenticator authenticator, MailboxACLResolver aclResolver,
			GroupMembershipResolver groupMembershipResolver) {
		super(mailboxSessionMapperFactory, authenticator, aclResolver,
				groupMembershipResolver);
	}

	// Convenience method for use with spring
	public void startup() {
		try {
			init();
		} catch (MailboxException e) {
			LOGGER.warn("Could not initialize Enkive mailbox manager", e);
		}
	}

}
