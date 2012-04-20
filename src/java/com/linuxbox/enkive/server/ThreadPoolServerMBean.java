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
package com.linuxbox.enkive.server;

public interface ThreadPoolServerMBean {
	/**
	 * Returns the number of tasks currently enqueued.
	 * 
	 * @return
	 */
	public int getEnqueuedCount();

	/**
	 * Returns an approximation how many tasks are being actively run by
	 * threads.
	 * 
	 * @return
	 */
	public int getActiveCount();

	/**
	 * Returns an approximation the number of tasks currently in the system --
	 * either active or enqueued.
	 * 
	 * @return
	 */
	public long getTaskCount();

	/**
	 * Returns the current size of the thread pool.
	 * 
	 * @return
	 */
	public int getPoolSize();

	/**
	 * Returns minimum (core) size the thread pool is allowed to be.
	 * 
	 * @return
	 */
	public int getCorePoolSize();

	/**
	 * Returns maximum size thread pool is allowed to be.
	 * 
	 * @return
	 */
	public int getMaximumPoolSize();

	/**
	 * Returns historic largest size thread pool ever was as it had the
	 * opportunity to shift between core pool size and maximum pool size.
	 * 
	 * @return
	 */
	public int getLargestPoolSize();
}
