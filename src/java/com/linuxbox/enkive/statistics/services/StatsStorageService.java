/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.statistics.services;

import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface StatsStorageService {

	/**
	 * Store all statistics contained within a set. The format of each map is
	 * retained when it is stored.
	 * 
	 * @param dataSet
	 * @throws StatsStorageException
	 */
	void storeStatistics(List<Map<String, Object>> dataSet)
			throws StatsStorageException;

	/**
	 * Store a map after appending a service name to it
	 * 
	 * @param service
	 *            -- gatherer name
	 * @param rawStats
	 *            -- map to store
	 * @throws StatsStorageException
	 */
	void storeStatistics(String service, RawStats rawStats)
			throws StatsStorageException;
}
