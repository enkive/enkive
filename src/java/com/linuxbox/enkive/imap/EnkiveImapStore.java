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
package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.mail.ModSeqProvider;
import org.apache.james.mailbox.store.mail.UidProvider;
import org.apache.james.mailbox.store.mail.model.Mailbox;

public abstract class EnkiveImapStore implements UidProvider<String>,
		ModSeqProvider<String> {

	@Override
	public long nextModSeq(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return 1;
	}

	@Override
	public long highestModSeq(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return 1;
	}

	@Override
	public long nextUid(MailboxSession session, Mailbox<String> mailbox)
			throws MailboxException {
		return lastUid(session, mailbox) + 1;
	}

}
