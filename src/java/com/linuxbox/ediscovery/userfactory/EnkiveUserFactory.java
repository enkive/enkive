/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.ediscovery.userfactory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.AbstractUserFactory;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.AuthenticatingConnector;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;

import com.linuxbox.ediscovery.connector.EnkiveAuthenticator;

public class EnkiveUserFactory extends AbstractUserFactory {

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.ediscovery.authentication");

	protected String authenticationEndpoint;
	public final static String ENKIVE_USERDETAILS_URL = "/user/permissions";
	public static final String USER_AUTHORITIES = "userAuthorities";

	public EnkiveUserFactory(String authenticationEndpoint) {
		this.authenticationEndpoint = authenticationEndpoint;
	}

	@Override
	public boolean authenticate(HttpServletRequest request, String username,
			String password) {
		boolean authenticated = false;
		try {
			// make sure our credentials are in the vault

			CredentialVault vault = frameworkUtils.getCredentialVault(
					request.getSession(), username);
			Credentials credentials = vault
					.newCredentials(authenticationEndpoint);
			credentials.setProperty(Credentials.CREDENTIAL_USERNAME, username);
			credentials.setProperty(Credentials.CREDENTIAL_PASSWORD, password);

			// build a connector whose connector session is bound to the current
			// session
			Connector connector = frameworkUtils.getConnector(
					request.getSession(), username, authenticationEndpoint);
			connector.setCredentials(credentials);
			AuthenticatingConnector authenticatingConnector;
			if (connector instanceof AuthenticatingConnector) {
				authenticatingConnector = (AuthenticatingConnector) connector;
			} else {
				authenticatingConnector = new AuthenticatingConnector(
						connector, new EnkiveAuthenticator());
			}
			if (authenticatingConnector != null) {
				authenticated = authenticatingConnector.handshake();
			}

		} catch (Exception ex) {
			logger.warn("Exception in EnkiveUserFactory.authenticate()", ex);
		}
		return authenticated;
	}

	@Override
	public User loadUser(RequestContext arg0, String arg1)
			throws UserFactoryException {
		return loadUser(arg0, arg1, null);
	}

	@Override
	public User loadUser(RequestContext cxt, String userId, String endpointId)
			throws UserFactoryException {
		String currentUserId = cxt.getUserId();
		if (currentUserId == null) {
			currentUserId = userId;
		}

		Connector connector;
		try {
			connector = frameworkUtils
					.getConnector(cxt, authenticationEndpoint);
			// invoke and check for OK response
			Response response = connector.call(ENKIVE_USERDETAILS_URL);
			if (Status.STATUS_OK != response.getStatus().getCode()) {
				Exception cause;
				if (response.getStatus().getException() instanceof Exception) {
					cause = (Exception) response.getStatus().getException();
				} else {
					cause = new Exception("see cause", response.getStatus()
							.getException());
				}

				throw new UserFactoryException(
						"Unable to create user - failed to retrieve user metadata; code="
								+ response.getStatus().getCode() + "; "
								+ response.getStatus().getMessage(), cause);
			}

			// Load the user properties via the JSON parser
			JSONObject jsonAuthorities = new JSONObject(response.getResponse());

			JSONArray authArray = jsonAuthorities
					.getJSONArray(USER_AUTHORITIES);

			Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
			capabilities.put("isAdmin", false);
			for (int i = 0; i < authArray.length(); i++) {
				String auth = authArray.getString(i);
				if (auth.equals("ROLE_ENKIVE_ADMIN")) {
					capabilities.put("isAdmin", true);
				} else {
					capabilities.put(auth, true);
				}
			}
			User user = new User(userId, capabilities);
			return user;
		} catch (ConnectorServiceException e) {
			e.printStackTrace();
			throw new UserFactoryException(
					"Cannot connect to enkive to load user", e);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new UserFactoryException(
					"Could not parse JSON returned from enkive to load user", e);
		}
	}

}
