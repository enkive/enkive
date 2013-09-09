package com.linuxbox.enkive.imap.mailbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mailbox.store.MailboxEventDispatcher;
import org.apache.james.mailbox.store.SimpleMailboxSession;
import org.slf4j.Logger; // XXX fix this

import com.linuxbox.enkive.workspace.Workspace;


public class EnkiveMailboxSession extends SimpleMailboxSession {
	
	private Workspace workspace;
	private MailboxEventDispatcher<String> dispatcher;
	private Map<String, EnkiveImapMailbox> mailboxes;

	public EnkiveMailboxSession(final long sessionId, final String userName, final
			String password, final Logger log, final List<Locale> localePreferences, char
			pathSeparator, SessionType type, Workspace workspace,
			MailboxEventDispatcher<String> dispatcher) {

		super(sessionId, userName, password, log, localePreferences, new
				ArrayList<String>(), null, pathSeparator, type);
		this.workspace = workspace;
		this.dispatcher = dispatcher;
		this.mailboxes = new HashMap<String, EnkiveImapMailbox>();
	}

    public Workspace getWorkspace() {
    	return workspace;
    }

    public void setWorkspace(Workspace workspace) {
    	this.workspace = workspace;
    }

    public MailboxEventDispatcher<String> getEventDispatcher() {
	return dispatcher;
    }

    public void setEventDispatcher(MailboxEventDispatcher<String> dispatcher) {
	this.dispatcher = dispatcher;
    }

    public void addMailbox(EnkiveImapMailbox mailbox) {
	if (mailboxes.containsKey(mailbox.getName())) {
		return;
	}

	mailboxes.put(mailbox.getName(), mailbox);
    }

    public EnkiveImapMailbox findMailbox(String name) {
	return (mailboxes.get(name));
    }

    /**
     * Terminate all of our open mailboxes that are tracking searches.
     */
    @Override
    public void close() {
	for (EnkiveImapMailbox mailbox : mailboxes.values()) {
		mailboxes.remove(mailbox.getName());
		mailbox.close();
	}

        super.close();
    }
}
