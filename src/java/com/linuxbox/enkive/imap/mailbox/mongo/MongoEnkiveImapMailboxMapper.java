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
package com.linuxbox.enkive.imap.mailbox.mongo;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxConstants;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.impl.SimpleMailbox;

import com.linuxbox.enkive.imap.Constants;
import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailbox;
import com.linuxbox.enkive.imap.mailbox.EnkiveImapMailboxMapper;
import com.linuxbox.enkive.imap.mailbox.EnkiveMailboxSession;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

public class MongoEnkiveImapMailboxMapper extends EnkiveImapMailboxMapper {
	private Workspace workspace;
	long searchUpdateInterval;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.imap.mailbox.mongo");

	public MongoEnkiveImapMailboxMapper(MailboxSession session,
			long searchUpdateInterval) {
		super(session);
		this.workspace = ((EnkiveMailboxSession) session).getWorkspace();
		this.searchUpdateInterval = searchUpdateInterval;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public long getSearchUpdateInterval() {
		return searchUpdateInterval;
	}

	public void setSearchUpdateInterval(long searchUpdateInterval) {
		this.searchUpdateInterval = searchUpdateInterval;
	}

	@Override
	public synchronized List<Mailbox<String>> list() throws MailboxException {
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		EnkiveMailboxSession esession = (EnkiveMailboxSession) session;

		try {
			for (SearchQuery search : workspace.getSearches()) {
				if (search.isIMAP()) {
					EnkiveImapMailbox mailbox = esession.findMailbox(search
							.getName());
					if (mailbox == null) {
						MailboxPath mailboxPath = new MailboxPath(
								session.getPersonalSpace(), session.getUser()
										.getUserName(), search.getName());
						mailbox = new EnkiveImapMailbox(mailboxPath, search,
								esession);
						mailbox.setMailboxId(search.getId());
						mailbox.setSearchUpdateInterval(searchUpdateInterval);
						esession.addMailbox(mailbox);
					}
					mailboxes.add(mailbox);
				}
			}
		} catch (WorkspaceException e) {
			throw new MailboxException("Failed to get workspace/queries", e);
		}

		mailboxes.add(new SimpleMailbox<String>(new MailboxPath(session
				.getPersonalSpace(), session.getUser().getUserName(),
				MailboxConstants.INBOX), 1));
		mailboxes.add(new SimpleMailbox<String>(new MailboxPath(session
				.getPersonalSpace(), session.getUser().getUserName(),
				Constants.MAILBOX_TRASH), 1));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result of MongoEnkiveImapMailboxMapper.list for user \""
					+ esession.getUserName()
					+ "\", workspace \""
					+ workspace.getWorkspaceName() + "\"");
			logMailboxes(mailboxes);
		}

		return mailboxes;
	}

	@Override
	public synchronized Mailbox<String> findMailboxByPath(
			MailboxPath mailboxName) throws MailboxException,
			MailboxNotFoundException {
		Mailbox<String> result;

		EnkiveMailboxSession esession = (EnkiveMailboxSession) session;
		if (mailboxName.getName().equals(MailboxConstants.INBOX)) {
			MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), MailboxConstants.INBOX);
			result = new SimpleMailbox<String>(inboxPath, 1);
		} else if (mailboxName.getName().equals(Constants.MAILBOX_TRASH)) {
			MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), Constants.MAILBOX_TRASH);
			result = new SimpleMailbox<String>(trashPath, 1);
		} else {
			try {
				// force search to only return IMAP results since due to quirk
				// of saving, it's possible to have multiple mailboxes with same
				// name, some being IMAP others not; multiple mailboxes of same
				// name should be prevented, but until then, at least insure we
				// get an IMAP box
				SearchQuery search = workspace.getSearchQueryBuilder()
						.getSearchQueryByNameAndImap(mailboxName.getName(),
								true);
				if (search == null) {
					throw new MailboxNotFoundException(mailboxName);
				}

				EnkiveImapMailbox mailbox = esession.findMailbox(search
						.getName());
				if (mailbox == null) {
					MailboxPath mailboxPath = new MailboxPath(
							session.getPersonalSpace(), session.getUser()
									.getUserName(), search.getName());
					mailbox = new EnkiveImapMailbox(mailboxPath, search,
							esession);
					mailbox.setMailboxId(search.getId());
					mailbox.setSearchUpdateInterval(searchUpdateInterval);
					esession.addMailbox(mailbox);
				}
				result = mailbox;
			} catch (WorkspaceException e) {
				throw new MailboxNotFoundException(mailboxName);
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result of MongoEnkiveImapMailboxMapper.findMailboxByPath user \""
					+ esession.getUserName()
					+ "\", workspace \""
					+ workspace.getWorkspaceName()
					+ "\", path \""
					+ mailboxName + "\"");
			logMailbox(result);
		}

		return result;
	}

	@Override
	public synchronized List<Mailbox<String>> findMailboxWithPathLike(
			MailboxPath mailboxPath) {
		String mailboxSearchPath = mailboxPath.getName();
		ArrayList<Mailbox<String>> mailboxes = new ArrayList<Mailbox<String>>();
		EnkiveMailboxSession esession = (EnkiveMailboxSession) session;
		if (mailboxSearchPath.equals("%"))
			try {
				return list();
			} catch (MailboxException e) {
				LOGGER.error("Error retrieving list of mailboxes for user "
						+ session.getUser().getUserName(), e);
			}
		else if (mailboxSearchPath.matches(MailboxConstants.INBOX)) {
			MailboxPath inboxPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), MailboxConstants.INBOX);
			mailboxes.add(new SimpleMailbox<String>(inboxPath, 1));
		} else if (mailboxSearchPath.matches(Constants.MAILBOX_TRASH)) {
			MailboxPath trashPath = new MailboxPath(session.getPersonalSpace(),
					session.getUser().getUserName(), Constants.MAILBOX_TRASH);
			mailboxes.add(new SimpleMailbox<String>(trashPath, 1));
		} else {
			String regex = mailboxSearchPath.replace(".", "+\\.+");
			regex = regex.replace('%', '.') + "*";
			try {
				for (SearchQuery search : workspace.getSearches()) {
					if (search.isIMAP()) {
						String imapName = search.getName().replace('/', '.');
						if (imapName.matches(regex)) {
							EnkiveImapMailbox mailbox = esession
									.findMailbox(search.getName());
							if (mailbox == null) {
								MailboxPath matchPath = new MailboxPath(
										session.getPersonalSpace(), session
												.getUser().getUserName(),
										search.getName());
								mailbox = new EnkiveImapMailbox(matchPath,
										search, esession);
								mailbox.setMailboxId(search.getId());
								mailbox.setSearchUpdateInterval(searchUpdateInterval);
								esession.addMailbox(mailbox);
							}
							mailboxes.add(mailbox);
						}
					}
				}
			} catch (WorkspaceException e) {
				// Fallthrough
			} catch (MailboxException e) {
				// Fallthrough
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result of MongoEnkiveImapMailboxMapper.findMailboxWithPathLike \""
					+ mailboxPath.getName()
					+ "\", user \""
					+ esession.getUserName()
					+ "\"/\""
					+ mailboxPath.getUser()
					+ "\"");
			logMailboxes(mailboxes);
		}

		return mailboxes;
	}

	@Override
	public boolean hasChildren(Mailbox<String> mailbox, char delimiter)
			throws MailboxException, MailboxNotFoundException {
		// No children in this layout
		return false;
	}

	protected static void logMailboxes(List<Mailbox<String>> mailboxes) {
		for (Mailbox<String> m : mailboxes) {
			logMailbox(m);
		}
	}

	protected static void logMailbox(Mailbox<String> m) {
		LOGGER.debug(m.getUser() + " / " + m.getName() + " / "
				+ m.getMailboxId());
	}
}
