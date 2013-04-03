/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
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
		throw new RuntimeException(
				"com.linuxbox.enkive.authentication.SpringSecurityAuthenticationService.setUserParentGroup called; had auto-generated stub.");
	}

	@Override
	public void setGroupParentGroup(String groupId, String parentGroupId) {
		throw new RuntimeException(
				"com.linuxbox.enkive.authentication.SpringSecurityAuthenticationService.setGroupParentGroup called; had auto-generated stub.");
	}

	@Override
	public boolean isValid(Session session) {
		return session != null && !session.getId().isEmpty();
	}

	@Override
	public void deathenticate(Session session) {
		session.invalidate();
	}

	@Override
	public void authenticate(String userID, String password) {
		throw new RuntimeException(
				"com.linuxbox.enkive.authentication.SpringSecurityAuthenticationService.authenticate called; had auto-generated stub.");
	}
}
