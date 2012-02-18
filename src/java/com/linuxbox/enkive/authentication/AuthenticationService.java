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
 ******************************************************************************/
package com.linuxbox.enkive.authentication;

import org.eclipse.jetty.server.session.AbstractSessionManager.Session;

public interface AuthenticationService {
	String getUserName() throws AuthenticationException;

	boolean addUser(String userId, String password);

	boolean addGroup(String groupId);

	void setUserParentGroup(String userId, String parentGroupId);

	void setGroupParentGroup(String groupId, String parentGroupId);

	boolean isValid(Session session);

	void deathenticate(Session session);

	void authenticate(String userID, String password);
}
