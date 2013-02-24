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
package com.linuxbox.ediscovery.connector;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.extensions.webscripts.connector.RemoteClient;

public class EnkiveRemoteClient extends RemoteClient {

	protected String enkiveAuthenticationUrl;
	protected boolean authenticated = true;
	protected String defaultEncoding = null;

	public EnkiveRemoteClient(String endpoint) {
		super(endpoint);
	}
	
	public EnkiveRemoteClient(String endpoint, String defaultEncoding) {
		super(endpoint, defaultEncoding);
		this.defaultEncoding = defaultEncoding;
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

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

}
