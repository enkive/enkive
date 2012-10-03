package com.linuxbox.enkive.imap;

import java.util.Date;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public interface EnkiveImapAccountCreator {

	public boolean accountExists(String username);

	public void createImapAccount(String username)
			throws MessageSearchException;

	public void addImapMessages(String username, Date fromDate, Date toDate)
			throws MessageSearchException;

}
