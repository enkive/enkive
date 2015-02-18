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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MailboxSession.SessionType;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.store.Authenticator;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;
import org.slf4j.Logger;

import com.linuxbox.enkive.imap.mailbox.mongo.MongoEnkiveMailboxSessionMapperFactory;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;

public class EnkiveMailboxManager extends StoreMailboxManager<String> {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap");
	private LinkedList<EnkiveMailboxSession> sessions;

	public EnkiveMailboxManager(
			MailboxSessionMapperFactory<String> mailboxSessionMapperFactory,
			Authenticator authenticator, MailboxACLResolver aclResolver,
			GroupMembershipResolver groupMembershipResolver) {
		super(mailboxSessionMapperFactory, authenticator, aclResolver,
				groupMembershipResolver);

		sessions = new LinkedList<EnkiveMailboxSession>();
	}

    /**
     * Create Session (overrides SimpleMailboxSession)
     * 
     * @param userName
     * @param log
     * @return session
     */
    protected MailboxSession createSession(String userName, String password,
		Logger log, SessionType type) {
	MongoEnkiveMailboxSessionMapperFactory factory =
			(MongoEnkiveMailboxSessionMapperFactory)getMapperFactory();
	Workspace workspace;
	try {
			workspace = factory.getWorkspaceService().getActiveWorkspace(userName);
		} catch (WorkspaceException e) {
			return null;
		}

	EnkiveMailboxSession session = new EnkiveMailboxSession(randomId(), userName,
			password, log, new ArrayList<Locale>(), getDelimiter(), type, workspace,
			getEventDispatcher());
	sessions.add(session);

	return session;
    }

    /**
     * Override logout to remove session from list
     */
    @Override
    public void logout(MailboxSession session, boolean force) throws MailboxException {
        if (session != null) {
		sessions.remove(session);
            super.logout(session, force);
        }
    }

	// Convenience method for use with spring
	public void startup() {
		try {
			init();
		} catch (MailboxException e) {
			LOGGER.warn("Could not initialize Enkive mailbox manager", e);
		}
	}

	public void shutdown() {
		EnkiveMailboxSession session;
		try {
			while ((session = sessions.pop()) != null) {
				session.close();
			}
		} catch (NoSuchElementException e) {
			// Nothing to do; list was empty
		}
	}
}
