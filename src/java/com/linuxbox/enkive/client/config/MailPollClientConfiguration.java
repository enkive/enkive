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
 *******************************************************************************/
package com.linuxbox.enkive.client.config;

import javax.mail.URLName;

public class MailPollClientConfiguration {
	private String protocol;
	private String host;
	private int port;
	private String user;
	private String password;
	private String folder;
	private int timeout;
	private boolean enabled;
	
	public MailPollClientConfiguration() {
		super();
		this.protocol = null;
		this.host = null;
		this.port = -1;
		this.user = null;
		this.password = null;
		this.folder = null;
		this.timeout = 120000; // 2 minutes by default
		this.enabled = false;
	}

	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getFolder() {
		return folder;
	}
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public URLName getURL() {
		return new URLName(this.protocol, this.host, this.port, null, this.user, this.password);
	}

}
