package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;
import com.mongodb.MongoException;

public class StatsMsgSearchGatherer extends AbstractGatherer {
	MessageSearchService searchService;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering");
	
	public StatsMsgSearchGatherer(String serviceName, String schedule) {
		super(serviceName, schedule);
	}
	
	protected Map<String, Set<String>> keyBuilder(){
		Map<String, Set<String>> keys = new HashMap<String, Set<String>>();
		keys.put(STAT_SERVICE_NAME, null);
		keys.put(STAT_NUM_ENTRIES, makeCreator(GRAIN_AVG));
		keys.put(STAT_TIME_STAMP, makeCreator(GRAIN_AVG, GRAIN_MAX, GRAIN_MIN));
		return keys;
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

	public Map<String, Object> getStatistics() {
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		Date prevDate = new Date(currTime - THIRTY_DAYS);
		return getStatistics(prevDate, currDate);
	}

	// testing
	public Map<String, Object> getStatistics(Date startDate, Date endDate) {
		Map<String, Object> result = createMap();
		// create value strings for current date and 30-days previous
		String lowerDate = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String upperDate = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();

		int numEntries = numEntries(lowerDate, upperDate);
		
		if(numEntries < 0){
			return null;
		}
		
		result.put(STAT_TIME_STAMP, System.currentTimeMillis());
		result.put(STAT_NUM_ENTRIES, numEntries);
		return result;
	}

	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsMsgSearchGatherer msgEntries = new StatsMsgSearchGatherer("name", "0/5 * * * ?");
		System.out.println(msgEntries.getStatistics());
		String[] keys = {};
		System.out.println(msgEntries.getStatistics(keys));
	}
}