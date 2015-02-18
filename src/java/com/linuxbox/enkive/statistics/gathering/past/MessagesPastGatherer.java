/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.statistics.gathering.past;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MSGS;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.StatsMessageGatherer;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class MessagesPastGatherer extends PastGatherer {
	GathererMessageSearchService searchService;
	protected StatsMessageGatherer msgGatherer;

	public MessagesPastGatherer(GathererMessageSearchService searchService,
			String name, StatsClient client, StatsMessageGatherer msgGatherer,
			int hrKeepTime, int dayKeepTime, int weekKeepTime, int monthKeepTime) {
		super(name, client, hrKeepTime, dayKeepTime, weekKeepTime,
				monthKeepTime);
		this.searchService = searchService;
		this.msgGatherer = msgGatherer;
	}

	@PostConstruct
	public void init() {
		consolidatePastHours();
		consolidatePastDays();
		consolidatePastWeeks();
		consolidatePastMonths();
	}

	protected Map<String, Object> getConsolidatedData(Date start, Date end,
			int grain) throws GathererException {
		Map<String, Object> result = new HashMap<String, Object>();
		int numEntries = 0;
		int totalMsgs = 0;
		try {
			numEntries = searchService.getNumberOfMessages(start, end);
			totalMsgs = searchService.getNumberOfMessages(new Date(0L), end);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}

		if (totalMsgs == 0) {
			return null;
		}

		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(CONSOLIDATION_MIN, start);
		dateMap.put(CONSOLIDATION_MAX, end);

		Map<String, Object> innerNumEntries = new HashMap<String, Object>();
		innerNumEntries.put(CONSOLIDATION_AVG, numEntries);
		Map<String, Object> innerTotalMsgs = new HashMap<String, Object>();
		innerTotalMsgs.put(CONSOLIDATION_AVG, totalMsgs);

		result.put(CONSOLIDATION_TYPE, grain);
		result.put(STAT_TIMESTAMP, dateMap);
		result.put(STAT_GATHERER_NAME, gathererName);
		result.put(STAT_TOTAL_MSGS, innerTotalMsgs);
		result.put(STAT_NUM_ENTRIES, innerNumEntries);
		return result;
	}
}
