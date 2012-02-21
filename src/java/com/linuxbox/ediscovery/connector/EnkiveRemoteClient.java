package com.linuxbox.ediscovery.connector;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.extensions.webscripts.connector.RemoteClient;

public class EnkiveRemoteClient extends RemoteClient {

	protected String enkiveAuthenticationUrl;
	protected boolean authenticated = true;

	public EnkiveRemoteClient(String endpoint) {
		super(endpoint);
	}

	protected URL processResponse(URL url,
			org.apache.commons.httpclient.HttpMethod method)
			throws MalformedURLException {
		URL processedUrl = super.processResponse(url, method);
		if (processedUrl != null
				&& processedUrl.toString().startsWith(
						getEndpoint() + getEnkiveAuthenticationUrl())) {
			// Being redirected to login, not authenticated
			setAuthenticated(false);
		}
		return processedUrl;
	}

	public String getEnkiveAuthenticationUrl() {
		return enkiveAuthenticationUrl;
	}

	public void setEnkiveAuthenticationUrl(String enkiveAuthenticationUrl) {
		this.enkiveAuthenticationUrl = enkiveAuthenticationUrl;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

}
