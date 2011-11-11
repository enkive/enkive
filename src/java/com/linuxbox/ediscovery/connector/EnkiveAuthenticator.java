package com.linuxbox.ediscovery.connector;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.connector.AbstractAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.RemoteClient;

public class EnkiveAuthenticator extends AbstractAuthenticator {

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.ediscovery.authentication");

	public final static String ENKIVE_SESSION_TICKET = "JSESSIONID";
	public final static String ENKIVE_AUTHENTICATION_URL = "/j_spring_security_check";
	public final static String ENKIVE_LOGIN_USERNAME_FIELD = "j_username";
	public final static String ENKIVE_LOGIN_PASSWORD_FIELD = "j_password";

	public final static String HTTP_CONTENT_TYPE = "Content-type";
	public final static String HTTP_URL_ENCODED_FORM = "application/x-www-form-urlencoded";
	public final static String HTTP_SET_COOKIE = "set-cookie";
	public final static String HTTP_POST = "POST";

	public EnkiveAuthenticator() {

	}

	@Override
	public ConnectorSession authenticate(String endpoint,
			Credentials credentials, ConnectorSession connectorSession)
			throws AuthenticationException {
		ConnectorSession cs = null;

		String user, pass;
		if (credentials != null
				&& (user = (String) credentials
						.getProperty(Credentials.CREDENTIAL_USERNAME)) != null
				&& (pass = (String) credentials
						.getProperty(Credentials.CREDENTIAL_PASSWORD)) != null) {

			// build a new remote client
			RemoteClient remoteClient = new RemoteClient(endpoint);
			URL url;
			try {
				url = new URL(remoteClient.getURL() + ENKIVE_AUTHENTICATION_URL);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty(HTTP_CONTENT_TYPE,
						HTTP_URL_ENCODED_FORM);
				conn.setRequestMethod(HTTP_POST);
				BufferedOutputStream os = new BufferedOutputStream(
						conn.getOutputStream());
				String body = ENKIVE_LOGIN_USERNAME_FIELD + "="
						+ URLEncoder.encode(user) + "&"
						+ ENKIVE_LOGIN_PASSWORD_FIELD + "="
						+ URLEncoder.encode(pass);
				os.write(body.getBytes());
				os.close();
				if (!conn.getHeaderField("Location").endsWith("login_error")) {
					setCookie(conn.getHeaderField(HTTP_SET_COOKIE),
							connectorSession);
					cs = connectorSession;
				}

			} catch (MalformedURLException e) {
				logger.error("Could not authenticate to Enkive - URL: "
						+ remoteClient.getURL() + ENKIVE_AUTHENTICATION_URL, e);
			} catch (IOException e) {
				logger.error("Could not authenticate to Enkive - URL: "
						+ remoteClient.getURL() + ENKIVE_AUTHENTICATION_URL, e);
			}

			if (logger.isDebugEnabled())
				logger.debug("Authenticating user: " + user);

		} else if (logger.isDebugEnabled()) {
			logger.debug("No user credentials available - cannot authenticate.");
		}

		return cs;

	}

	@Override
	public boolean isAuthenticated(String endpoint,
			ConnectorSession connectorSession) {
		return (connectorSession != null && connectorSession
				.getCookie(ENKIVE_SESSION_TICKET) != null);

	}

	private void setCookie(String headerValue, ConnectorSession connectorSession) {

		int z = headerValue.indexOf("=");
		if (z > -1) {
			String cookieName = (String) headerValue.substring(0, z);
			String cookieValue = (String) headerValue.substring(z + 1,
					headerValue.length());
			int y = cookieValue.indexOf(";");
			if (y > -1) {
				cookieValue = cookieValue.substring(0, y);
			}
			if (connectorSession != null) {
				connectorSession.setCookie(cookieName, cookieValue);
			}
		}
	}

}
