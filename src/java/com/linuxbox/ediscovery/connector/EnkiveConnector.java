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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.EndpointManager;
import org.springframework.extensions.webscripts.connector.HttpConnector;
import org.springframework.extensions.webscripts.connector.RemoteClient;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;

public class EnkiveConnector extends HttpConnector {

	public EnkiveConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	@Override
	public Response call(String uri, ConnectorContext context, InputStream in) {
		Response response;
		if (EndpointManager.allowConnect(this.endpoint)) {
			EnkiveRemoteClient remoteClient = initRemoteClient(context);

			// call client and process response
			response = new EnkiveResponseFacade(remoteClient.call(uri, false, in));

			if (!remoteClient.isAuthenticated()) {
				ResponseStatus status = new ResponseStatus();
				status.setCode(ResponseStatus.STATUS_FORBIDDEN);
				response = new Response(status);
			} else {
				processResponse(remoteClient, response);
			}
		} else {
			ResponseStatus status = new ResponseStatus();
			status.setCode(ResponseStatus.STATUS_INTERNAL_SERVER_ERROR);
			response = new Response(status);
		}

		return response;
	}

	@Override
	public Response call(String uri, ConnectorContext context) {
		return call(uri, context, null);
	}

	@Override
	protected void applyRequestAuthentication(RemoteClient remoteClient,
			ConnectorContext context) {

		if (getConnectorSession() != null) {
			Map<String, String> cookies = new HashMap<String, String>();
			for (String key : getConnectorSession().getCookieNames()) {
				cookies.put(key, getConnectorSession().getCookie(key));
			}
			remoteClient.setCookies(cookies);
		}

		Map<String, String> headers = new HashMap<String, String>(8);
		if (context != null) {
			headers.putAll(context.getHeaders());
		}
		if (headers.size() != 0) {
			remoteClient.setRequestProperties(headers);
		}
	}

	protected EnkiveRemoteClient initRemoteClient(ConnectorContext context) {
		// create a remote client
		EnkiveRemoteClient remoteClient = new EnkiveRemoteClient(getEndpoint());
		remoteClient
				.setEnkiveAuthenticationUrl(EnkiveAuthenticator.ENKIVE_LOGIN_URL);
		// configure the client
		if (context != null) {
			remoteClient.setRequestContentType(context.getContentType());
			remoteClient.setRequestMethod(context.getMethod());
		}

		applyRequestAuthentication(remoteClient, context);

		return remoteClient;
	}
}
