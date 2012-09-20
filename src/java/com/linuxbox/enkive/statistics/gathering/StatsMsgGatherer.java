package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MSGS;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererMessageSearchService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
public class StatsMsgGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMsgGatherer");
	protected Mongo m;
	protected DB db;
	protected DBCollection attachmentsColl;
	GathererMessageSearchService searchService;
	
	public StatsMsgGatherer(String serviceName, String humanName, List<String> keys) throws GathererException {
		super(serviceName, humanName,keys);
	}
	
	public void setSearchService(GathererMessageSearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		int totEntries = 0;
		
		try {
			totEntries = searchService.getNumberOfMessages(new Date(0L), endTimestamp);
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
			numEntries = searchService.getNumberOfMessages(startTimestamp, endTimestamp);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}
		
		Map<String, Object> intervalStats = createMap();
		intervalStats.put(STAT_NUM_ENTRIES, numEntries);
		return intervalStats;
	}
}

