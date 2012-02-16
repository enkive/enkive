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
package com.linuxbox.enkive.docsearch.indri;

import java.util.Collection;
import java.util.LinkedList;

import lemurproject.indri.QueryEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryEnvironmentManager {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");
	private final static boolean LOG_TIMINGS = false;

	private Collection<String> indexPaths;
	private Collection<String> indexServers;
	/**
	 * Number of milliseconds between refreshes.
	 */
	private long queryEnvironmentRefreshInterval;
	private QueryEnvironment queryEnvironment;
	private long createdAt;

	public QueryEnvironmentManager(long queryEnvironmentRefreshInterval) {
		this.queryEnvironmentRefreshInterval = queryEnvironmentRefreshInterval;
		this.queryEnvironment = null;
		this.createdAt = -1;
		this.indexPaths = new LinkedList<String>();
		this.indexServers = new LinkedList<String>();
	}

	public QueryEnvironment createQueryEnvironment() throws Exception {
		QueryEnvironment queryEnvironment = new QueryEnvironment();
		if (indexPaths != null) {
			for (String path : indexPaths) {
				queryEnvironment.addIndex(path);
			}
		}
		if (indexServers != null) {
			for (String server : indexServers) {
				queryEnvironment.addServer(server);
			}
		}
		return queryEnvironment;
	}

	/**
	 * Keep an active reference to the holder as long as you use the
	 * QueryEnvironment within. If you don't have a reference, then the holder
	 * can be garbage collected, which will cause the QueryEnvironment within to
	 * be closed.
	 * 
	 * @return
	 * @throws Exception
	 */
	public QueryEnvironment getQueryEnvironment() throws Exception {
		final long now = System.currentTimeMillis();
		synchronized (this) {
			if (queryEnvironment == null
					|| (now - createdAt >= queryEnvironmentRefreshInterval)) {
				queryEnvironment = createQueryEnvironment();
				createdAt = System.currentTimeMillis();
				if (LOG_TIMINGS) {
					if (LOGGER.isTraceEnabled())
						LOGGER.trace("took " + (createdAt - now) / 1000.0
								+ " seconds to create a QueryEnvironment");
				}
			}

			return queryEnvironment;
		}
	}

	public synchronized void forceQueryEnvironmentRefresh() {
		queryEnvironment = null;
	}

	public Collection<String> getIndexPaths() {
		return indexPaths;
	}

	public void setIndexPaths(Collection<String> indexPaths) {
		this.indexPaths = indexPaths;
	}

	public Collection<String> getIndexServers() {
		return indexServers;
	}

	public void setIndexServers(Collection<String> indexServers) {
		this.indexServers = indexServers;
	}

	public long getQueryEnvironmentRefreshInterval() {
		return queryEnvironmentRefreshInterval;
	}

	/**
	 * Set the number of milliseconds between QueryEnvironment refreshes.
	 * 
	 * @param refreshIntervalMilliseconds
	 */
	public void setQueryEnvironmentRefreshInterval(
			long refreshIntervalMilliseconds) {
		this.queryEnvironmentRefreshInterval = refreshIntervalMilliseconds;
	}

	public void addIndexPath(String path) {
		indexPaths.add(path);
	}

	public void addIndexServer(String server) {
		indexServers.add(server);
	}
}
