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
package com.linuxbox.enkive.imap.mailbox;

import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.MailboxMapper;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.transaction.NonTransactionalMapper;

public abstract class EnkiveImapMailboxMapper extends NonTransactionalMapper
		implements MailboxMapper<String> {

	protected MailboxSession session;

	public EnkiveImapMailboxMapper(MailboxSession session) {
		this.session = session;
	}

	@Override
	public void endRequest() {

	}

	@Override
	public void save(Mailbox<String> mailbox) throws MailboxException {

	}

	@Override
	public void delete(Mailbox<String> mailbox) throws MailboxException {

	}
	
	@Override
	public abstract Mailbox<String> findMailboxByPath(MailboxPath mailboxName)
			throws MailboxException, MailboxNotFoundException;

	@Override
	public abstract List<Mailbox<String>> findMailboxWithPathLike(
			MailboxPath mailboxPath);

	@Override
	public abstract boolean hasChildren(Mailbox<String> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException;

	@Override
	public abstract List<Mailbox<String>> list() throws MailboxException;

}
