package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_MSGS;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererMessageSearchService;
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
	
	public StatsMsgGatherer(String serviceName, String humanName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);
	}
	
	@Override
	public RawStats getStatistics() throws GathererException {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		Date endDate = cal.getTime();
		cal.add(Calendar.HOUR, -1);
		Date startDate = cal.getTime();
		
		return getStatistics(startDate, endDate);
	}
	
	public RawStats getStatistics(Date startDate, Date endDate) throws GathererException {
		int numEntries = 0;
		int totEntries = 0;
		
		try {
			numEntries = searchService.getNumberOfMessages(startDate, endDate);
			totEntries = searchService.getNumberOfMessages(new Date(0L), endDate);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}
		
		
		Map<String, Object> pointStats = VarsMaker.createMap();	
		pointStats.put(STAT_TOTAL_MSGS, totEntries);
		
		Map<String, Object> intervalStats = VarsMaker.createMap();
		intervalStats.put(STAT_NUM_ENTRIES, numEntries);

		RawStats result = new RawStats(intervalStats, pointStats, startDate, endDate);
		return result;
	}
	
	public void setSearchService(GathererMessageSearchService searchService) {
		this.searchService = searchService;
	}
}
