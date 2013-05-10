package com.linuxbox.enkive.imap.mailbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.james.mailbox.store.SimpleMailboxSession;
import org.slf4j.Logger; // XXX fix this
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


public class EnkiveMailboxSession extends SimpleMailboxSession {
	
	private SecurityContext context;

    public EnkiveMailboxSession(final long sessionId, final String userName, final String password,
            final Logger log, final List<Locale> localePreferences, char pathSeparator, SessionType type) {
    	
        super(sessionId, userName, password, log, localePreferences, new ArrayList<String>(),
        		null, pathSeparator, type);

        context = SecurityContextHolder.getContext(); // save authentication (ie., EnkiveUserDetails)
        SecurityContextHolder.clearContext(); // and clear thread-local saved crendentials
    }
    
    public SecurityContext getContext() {
    	return context;
    }
    
    public void setContext(SecurityContext ctx) {
    	context = ctx;
    }

}
