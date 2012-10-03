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

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import java.util.Date;
import java.util.Map;

import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Describes a query for the stats database. If specified, will return entries
 * at or later than the start time stamp and less than (not less than or equal
 * to) the end time stamp. If either time stamp is null, then the query will not
 * consider it.
 * 
 * @author eric
 * 
 */
public class MongoStatsDateQuery extends StatsQuery {
	public MongoStatsDateQuery(Date startTimestamp, Date endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}

	public Map<String, Object> getQuery() {
		Map<String, Object> mongoQuery = new BasicDBObject();
		DBObject time = new BasicDBObject();
		String tsStartKey;
		String tsEndKey;
		tsStartKey = STAT_TIMESTAMP + "." + CONSOLIDATION_MIN;
		tsEndKey = STAT_TIMESTAMP + "." + CONSOLIDATION_MAX;

		if (startTimestamp != null) {
			time = new BasicDBObject();
			time.put("$gte", startTimestamp);
			mongoQuery.put(tsStartKey, time);
		}
		if (endTimestamp != null) {
			time = new BasicDBObject();
			time.put("$lt", endTimestamp);
			mongoQuery.put(tsEndKey, time);
		}

		if (grainType != null) {
			if (grainType == 0) {
				mongoQuery.put(CONSOLIDATION_TYPE, null);
			} else {
				mongoQuery.put(CONSOLIDATION_TYPE, grainType);
			}
		}

		return mongoQuery;
	}
}
