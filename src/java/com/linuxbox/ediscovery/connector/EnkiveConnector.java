package com.linuxbox.ediscovery.connector;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.config.RemoteConfigElement.ConnectorDescriptor;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.HttpConnector;
import org.springframework.extensions.webscripts.connector.RemoteClient;

public class EnkiveConnector extends HttpConnector {

	public EnkiveConnector(ConnectorDescriptor descriptor, String endpoint) {
		super(descriptor, endpoint);
	}

	@Override
	protected void applyRequestAuthentication(RemoteClient remoteClient,
			ConnectorContext context) {
		
		if(getConnectorSession() != null){
			Map<String, String> cookies = new HashMap<String,String>();
			for(String key : getConnectorSession().getCookieNames()){
				cookies.put(key, getConnectorSession().getCookie(key));
			}
			remoteClient.setCookies(cookies);
		}
	}

}
