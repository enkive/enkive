package com.linuxbox.enkive.imap;

import org.apache.james.mailbox.model.MailboxMetaData;
import org.apache.james.mailbox.model.MailboxPath;

public class EnkiveMailboxMetaData implements MailboxMetaData {

	MailboxPath path;
	
	public EnkiveMailboxMetaData(MailboxPath path){
		this.path = path;
	}
	
	@Override
	public Children inferiors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Selectability getSelectability() {
		// TODO Auto-generated method stub
		return Selectability.NONE;
	}

	@Override
	public char getHierarchyDelimiter() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public MailboxPath getPath() {
		return path;
	}

}
