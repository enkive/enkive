/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TS_POINT;
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
 */
public class MongoStatsQuery extends StatsQuery {
	public String gathererName;
	public boolean isPointQuery;

	public MongoStatsQuery(String type, Date startTimestamp, Date endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		setIsPointQuery(type);
	}

	public MongoStatsQuery(String gathererName, Integer grainType, String type) {
		this.gathererName = gathererName;
		this.grainType = grainType;
		setIsPointQuery(type);
	}

	public MongoStatsQuery(String gathererName, String type,
			Date startTimestamp, Date endTimestamp) {
		this(type, startTimestamp, endTimestamp);
		this.gathererName = gathererName;
	}

	public MongoStatsQuery(String gathererName, Integer grainType) {
		this(gathererName, grainType, null);
	}

	public MongoStatsQuery(String gathererName, Integer grainType, String type,
			Date startTimestamp, Date endTimestamp) {
		this(gathererName, grainType, type);
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}

	public Map<String, Object> getQuery() {
		Map<String, Object> mongoQuery = new BasicDBObject();

		if (gathererName != null) {
			mongoQuery.put(STAT_GATHERER_NAME, gathererName);
		}

		if (grainType != null) {
			if (grainType == 0) {
				mongoQuery.put(CONSOLIDATION_TYPE, null);
			} else {
				mongoQuery.put(CONSOLIDATION_TYPE, grainType);
			}
		}

		DBObject time = new BasicDBObject();
		String tsKey;
		if (isPointQuery) {
			tsKey = STAT_TIMESTAMP + "." + STAT_TS_POINT;
		} else {
			tsKey = STAT_TIMESTAMP + "." + CONSOLIDATION_MIN;
		}

		if (startTimestamp != null) {
			time.put("$gte", startTimestamp);
		}
		if (endTimestamp != null) {
			time.put("$lt", endTimestamp);
		}

		if (!time.toMap().isEmpty()) {
			mongoQuery.put(tsKey, time);
		}

		return mongoQuery;
	}

	public void setIsPointQuery(String type) {
		if (type != null && type.equals(STAT_POINT)) {
			isPointQuery = true;
		} else {
			isPointQuery = false;// ie) consolidated
		}
	}
}
