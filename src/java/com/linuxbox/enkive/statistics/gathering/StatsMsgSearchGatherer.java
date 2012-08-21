package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.statistics.InstantRawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.RawStats;

public class StatsMsgSearchGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	long interval = 1000 * 60 * 60; // one hour (3600000 milliseconds)

	GathererMessageSearchService searchService;

	public StatsMsgSearchGatherer(String serviceName, String humanName,
			String schedule) {
		super(serviceName, humanName, schedule);
	}

	public StatsMsgSearchGatherer(String serviceName, String humanName,
			String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);

	}

	public GathererMessageSearchService getSearchService() {
		return searchService;
	}

	@Override
	public RawStats getStatistics() throws GathererException {
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		Date prevDate = new Date(currTime - interval);
		return getStatistics(prevDate, currDate);
	}

	/**
	 * used to get statistics corresponding to a particular time range conforms
	 * to standard of lower bound being greater than or equal to (inclusive) and
	 * upper bound of being less than (non-inclusive)
	 * 
	 * @param startDate
	 *            -lower bound date
	 * @param endDate
	 *            -upper bound date
	 * @return a map with statistics corresponding to the date range
	 * @throws GathererException
	 */
	public RawStats getStatistics(Date startDate, Date endDate)
			throws GathererException {
		int numEntries;
		try {
			numEntries = searchService.getNumberOfMessages(startDate, endDate);
		} catch (MessageSearchException e) {
			throw new GathererException(e);
		}
		Map<String, Object> stats = VarsMaker.createMap();
		stats.put(STAT_NUM_ENTRIES, numEntries);

		RawStats result = new InstantRawStats(stats, new Date());
		return result;
	}

	public void setSearchService(GathererMessageSearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * Set the number of milliseconds ...
	 * 
	 * @param interval
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}
}