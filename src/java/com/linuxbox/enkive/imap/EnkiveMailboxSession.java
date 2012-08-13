package com.linuxbox.enkive.imap;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.james.mailbox.MailboxSession;
import org.slf4j.Logger;

public class EnkiveMailboxSession implements MailboxSession, MailboxSession.User {

	Logger logger;
	
	public EnkiveMailboxSession(){
		logger = new org.slf4j.impl.Log4jLoggerFactory()
		.getLogger("com.linuxbox.enkive.imap.mailbox.session");
	}
	
	@Override
	public SessionType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getSessionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public Logger getLog() {
		return logger;
	}

	@Override
	public User getUser() {
		return this;
	}

	@Override
	public String getPersonalSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getOtherUsersSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getSharedSpaces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Object, Object> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getPathDelimiter() {
		return '.';
	}

	@Override
	public String getUserName() {
		// TODO Auto-generated method stub
		return "lee";
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return "test";
	}

	@Override
	public List<Locale> getLocalePreferences() {
		// TODO Auto-generated method stub
		return null;
	}

}
