package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;

public class StatsMsgSearchGatherer extends AbstractGatherer {
	long interval = 3600000;// one hour by default
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");

	MessageSearchService searchService;

	public StatsMsgSearchGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
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

	public Map<String, Object> getStatistics(Date startDate, Date endDate) {
		Map<String, Object> result = createMap();
		String lowerDate = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String upperDate = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();
		int numEntries = numEntries(lowerDate, upperDate);

		if (numEntries < 0) {
			return null;
		}

		result.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));
		result.put(STAT_NUM_ENTRIES, numEntries);
		return result;
	}

	protected int numEntries(String dateEarliest, String dateLatest) {
		HashMap<String, String> hmap = new HashMap<String, String>();
		hmap.put(DATE_EARLIEST_PARAMETER, dateEarliest);
		hmap.put(DATE_LATEST_PARAMETER, dateLatest);
		int result = -1;// -1 is error value
		try {
			// TODO is this efficient? should we instead add a method to our
			// search service that would do a count of messages b/w a pair of
			// dates?
			final SearchResult result2 = searchService.search(hmap);
			final Set<String> result3 = result2.getMessageIds();
			final int count = result3.size();
			if (count == 0) {
				LOGGER.warn("StatisticsMsgEntries: no Entries found between "
						+ dateEarliest + " & " + dateLatest);
			}
			result = count;
		} catch (MessageSearchException e) {
			LOGGER.warn(
					"MessageSearchException in StatsMsgEntries.numEntries()", e);
		} catch (NullPointerException e) {
			LOGGER.warn("NullPointerException in statsMsgEntries.numEntries()",
					e);
		}

		return result;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}
}