package com.linuxbox.enkive.authentication;

import org.eclipse.jetty.server.session.AbstractSessionManager.Session;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityAuthenticationService implements
		AuthenticationService {

	@Override
	public String getUserName() throws AuthenticationException {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	@Override
	public boolean addUser(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addGroup(String groupId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setUserParentGroup(String userId, String parentGroupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroupParentGroup(String groupId, String parentGroupId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isValid(Session session) {
		if(session != null && !session.getId().isEmpty())
			return true;
		else
			return false;
	}

	@Override
	public void deathenticate(Session session) {
		session.invalidate();

	}

	@Override
	public void authenticate(String userID, String password) {
		// TODO Auto-generated method stub

	}

}
