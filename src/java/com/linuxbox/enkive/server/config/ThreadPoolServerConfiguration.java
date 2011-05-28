/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.server.config;

import java.util.concurrent.TimeUnit;

public class ThreadPoolServerConfiguration {

	private int corePoolSize = 15;
	private int maximumPoolSize = 30;
	private long keepAliveTime = 60;
	private TimeUnit timeUnit = TimeUnit.SECONDS;
	private int queueSize = 100;

	public ThreadPoolServerConfiguration() {
		super();
	}

	public ThreadPoolServerConfiguration(int queueSize, int corePoolSize,
			int maximumPoolSize, long keepAliveTime, TimeUnit timeUnit) {
		super();
		this.queueSize = queueSize;
		this.corePoolSize = corePoolSize;
		this.maximumPoolSize = maximumPoolSize;
		this.keepAliveTime = keepAliveTime;
		this.timeUnit = timeUnit;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public int getQueueSize() {
		return queueSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
}
