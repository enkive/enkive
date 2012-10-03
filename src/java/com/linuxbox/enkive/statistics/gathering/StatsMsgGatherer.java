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
package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MSGS;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class StatsMsgGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMsgGatherer");
	protected Mongo m;
	protected DB db;
	protected DBCollection attachmentsColl;
	GathererMessageSearchService searchService;

	public StatsMsgGatherer(String serviceName, String humanName,
			List<String> keys) throws GathererException {
		super(serviceName, humanName, keys);
	}

	public void setSearchService(GathererMessageSearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		int totEntries = 0;

		try {
			totEntries = searchService.getNumberOfMessages(new Date(0L),
					endTimestamp);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}

		Map<String, Object> pointStats = createMap();
		pointStats.put(STAT_TOTAL_MSGS, totEntries);
		return pointStats;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		int numEntries = 0;

		try {
			numEntries = searchService.getNumberOfMessages(startTimestamp,
					endTimestamp);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}

		Map<String, Object> intervalStats = createMap();
		intervalStats.put(STAT_NUM_ENTRIES, numEntries);
		return intervalStats;
	}
}
