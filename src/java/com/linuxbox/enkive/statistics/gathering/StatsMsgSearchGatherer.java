package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public class StatsMsgSearchGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");
	
	long interval = 1000 * 60 * 60; // one hour (3600000 milliseconds) by default

	MessageSearchService searchService;

	public StatsMsgSearchGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
	}
	
	public StatsMsgSearchGatherer(String serviceName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, schedule, keys);		
	}

	public MessageSearchService getSearchService() {
		return searchService;
	}

	@Override
	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		Date prevDate = new Date(currTime - interval);
		return getStatistics(prevDate, currDate);
	}

	/**
	 * used to get statistics corresponding to a particular time range
	 * conforms to standard of lower bound being greater than or equal to 
	 * (inclusive) and upper bound of being less than (non-inclusive)
	 * @param startDate -lower bound date
	 * @param endDate -upper bound date
	 * @return a map with stats cooresponding to the date range
	 */
	public Map<String, Object> getStatistics(Date startDate, Date endDate) {
		Map<String, Object> result = createMap();
		String lowerDate = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String upperDate = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();
		int numEntries = -1;
		
		try {
			numEntries = numEntries(lowerDate, upperDate);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		result.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));
		result.put(STAT_NUM_ENTRIES, numEntries);
		return result;
	}

	protected int numEntries(String dateEarliest, String dateLatest) throws Exception {
		HashMap<String, String> hmap = new HashMap<String, String>();
		hmap.put(DATE_EARLIEST_PARAMETER, dateEarliest);
		hmap.put(DATE_LATEST_PARAMETER, dateLatest);
		int result = -1;// -1 is error value
		try {
			int count = searchService.countSearch(hmap);
			if (count == 0) {
				LOGGER.warn("StatisticsMsgEntries: no Entries found between "
						+ dateEarliest + " & " + dateLatest);
			}
			result = count;
		} catch (MessageSearchException e) {
			LOGGER.warn(
					"MessageSearchException in StatsMsgEntries.numEntries()", e);
			throw new GathererException("msgEntries crash");
		} catch (NullPointerException e) {
			LOGGER.warn("NullPointerException in statsMsgEntries.numEntries()",
					e);
			throw new GathererException("msgEntries crash");
		}
		
		if (result < 0){
			throw new GathererException("msgEntries crash");
		}

		return result;
	}

	public void setSearchService(MessageSearchService searchService) {
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