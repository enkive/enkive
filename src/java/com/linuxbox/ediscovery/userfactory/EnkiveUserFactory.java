package com.linuxbox.ediscovery.userfactory;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.AbstractUserFactory;
import org.springframework.extensions.webscripts.connector.AuthenticatingConnector;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.User;

import com.linuxbox.ediscovery.connector.EnkiveAuthenticator;

public class EnkiveUserFactory extends AbstractUserFactory {

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.ediscovery.authentication");

	protected String authenticationEndpoint;

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
			if(authenticatingConnector != null)
				authenticated = authenticatingConnector.handshake();

		} catch (Exception ex) {
			logger.warn("Exception in EnkiveUserFactory.authenticate()", ex);
		}
		return authenticated;
	}

	@Override
	public User loadUser(RequestContext arg0, String arg1)
			throws UserFactoryException {
		return null;
	}

	@Override
	public User loadUser(RequestContext cxt, String userId, String endpointId)
			throws UserFactoryException {
		Map<String, Boolean> capabilities = new HashMap<String, Boolean>();
		capabilities.put("isAdmin", true);
		User user = new User(userId, capabilities);
		return user;
	}

}
