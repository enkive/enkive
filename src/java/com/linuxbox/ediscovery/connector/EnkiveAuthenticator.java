package com.linuxbox.ediscovery.connector;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.exception.AuthenticationException;
import org.springframework.extensions.webscripts.connector.AbstractAuthenticator;
import org.springframework.extensions.webscripts.connector.ConnectorSession;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.RemoteClient;

public class EnkiveAuthenticator extends AbstractAuthenticator {

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.ediscovery.authentication");

	public final static String ENKIVE_SESSION_TICKET = "JSESSIONID";
	public final static String ENKIVE_AUTHENTICATION_URL = "/j_spring_security_check";

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
				url = new URL(remoteClient.getURL()
						+ ENKIVE_AUTHENTICATION_URL);
				HttpURLConnection.setFollowRedirects(false);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoOutput(true);
				conn.setRequestProperty("Content-type",
						"application/x-www-form-urlencoded");
				conn.setRequestMethod("POST");
				BufferedOutputStream os = new BufferedOutputStream(
						conn.getOutputStream());
				String body = "j_username=" + user + "&j_password=" + pass;
				os.write(body.getBytes());
				os.close();

				for (String key : conn.getHeaderFields().keySet()) {
					if (key != null && key.toLowerCase().equals("set-cookie")) {
						setCookie(conn.getHeaderField(key), connectorSession);
					}
				}
				cs = connectorSession;

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		return true;

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
