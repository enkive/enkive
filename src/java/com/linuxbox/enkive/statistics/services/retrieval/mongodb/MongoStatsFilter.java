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
package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;

public class MongoStatsFilter extends StatsFilter {

	public MongoStatsFilter(String gathererName, Map<String, Object> keys) {
		this.gathererName = gathererName;
		this.keys = keys;

		if (keys != null) {
			keys.put(STAT_GATHERER_NAME, 1);
			keys.put(STAT_TIMESTAMP, 1);
		}
	}

	@Override
	public Map<String, Object> getFilter() {
		return keys;
	}
}
