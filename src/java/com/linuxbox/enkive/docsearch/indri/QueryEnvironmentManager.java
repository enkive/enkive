package com.linuxbox.enkive.docsearch.indri;

import java.util.Collection;
import java.util.LinkedList;

import lemurproject.indri.QueryEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QueryEnvironmentManager {
	/**
	 * Holds a reference to a QueryEnvironment. IMPORTANT: keep an active
	 * reference to the QueryEnvironmentHolder as long as you use the
	 * QueryEnvironment within. If you don't maintain a reference, then the
	 * QueryEnvironmentHolder can be garbage collected, which will cause the
	 * QueryEnvironment within to be closed. Subsequent uses of the
	 * QueryEnvironment will throw exceptions and due to the JNI (Java Native
	 * Interface) nature of the QueryEnviornment, could bring down the JVM.
	 */
	interface QueryEnvironmentHolder {
		QueryEnvironment getQueryEnvironment();
	}

	/**
	 * The proxy acts as a holder. The QueryEnvironmentHolder interface exists
	 * to minimize how much of the QueryEnvironmentProxy is exposed.
	 * 
	 * @author ivancich
	 * 
	 */
	private class QueryEnvironmentProxy implements QueryEnvironmentHolder {
		private QueryEnvironment queryEnvironment;
		private long createdAt;

		private QueryEnvironmentProxy() throws Exception {
			queryEnvironment = new QueryEnvironment();
			LOGGER.trace("QueryEnvironment created: " + queryEnvironment);
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
			createdAt = System.currentTimeMillis();
		}

		public void finalize() {
			try {
				LOGGER.trace("about to close QueryEnvironment: " + queryEnvironment);
				queryEnvironment.close();
				LOGGER.trace("finished closing QueryEnvironment: " + queryEnvironment);
			} catch (Exception e) {
				LOGGER.warn("error closing query environment", e);
			}
		}

		public QueryEnvironment getQueryEnvironment() {
			return queryEnvironment;
		}

		private long getCreatedAt() {
			return createdAt;
		}
	}

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	private Collection<String> indexPaths;
	private Collection<String> indexServers;
	private long queryEnvironmentRefreshInterval;

	private QueryEnvironmentProxy queryEnvironmentProxy;

	public QueryEnvironmentManager(long queryEnvironmentRefreshInterval) {
		this.queryEnvironmentRefreshInterval = queryEnvironmentRefreshInterval;
		this.queryEnvironmentProxy = null;
		this.indexPaths = new LinkedList<String>();
		this.indexServers = new LinkedList<String>();
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
	public QueryEnvironmentHolder getQueryEnvironmentHolder() throws Exception {
		final long now = System.currentTimeMillis();
		synchronized (this) {
			if (queryEnvironmentProxy == null
					|| (now - queryEnvironmentProxy.getCreatedAt() >= queryEnvironmentRefreshInterval)) {
				queryEnvironmentProxy = new QueryEnvironmentProxy();
			}

			return queryEnvironmentProxy;
		}
	}

	public synchronized void forceQueryEnvironmentRefresh() {
		queryEnvironmentProxy = null;
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

	public void setQueryEnvironmentRefreshInterval(
			long queryEnvironmentRefreshInterval) {
		this.queryEnvironmentRefreshInterval = queryEnvironmentRefreshInterval;
	}

	public void addIndexPath(String path) {
		indexPaths.add(path);
	}

	public void addIndexServer(String server) {
		indexServers.add(server);
	}
}
