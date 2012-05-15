package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_ENTRIES;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.THIRTY_DAYS;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public class StatsMsgEntries implements StatsGatherer {
	// NOAH: I'd rather this searchService be called something like
	// msgSearchService, so we know what's being searched. Also, the Refactor
	// menu's Rename... item can do this automatically.
	MessageSearchService searchService;

	// NOAH: the log does not match the package this is actually in
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");

	// NOAH: lower and upper don't make it clear what they represent; maybe
	// dateEarliest and dateLatest would be better. Again refactor/rename will
	// work for the variables, getters, and setters.
	protected String lower, upper;

	public String getUpper() {
		return upper;
	}

	public String getLower() {
		return lower;
	}

	public void setUpper(String str) {
		upper = str;
	}

	public void setLower(String str) {
		lower = str;
	}

	public int numEntries() {
		HashMap<String, String> hmap = new HashMap<String, String>();
		hmap.put(DATE_EARLIEST_PARAMETER, lower);
		hmap.put(DATE_LATEST_PARAMETER, upper);
		int result = -1;// -1 is exception flag
		try {
			int count = searchService.search(hmap).getMessageIds().size();
			if (count == 0) {
				result = 0;
				LOGGER.warn("StatisticsMsgEntries: Warning no Entries found in numEntries() between "
						+ lower + " & " + upper);
			} else {
				result = count;
			}
		} catch (MessageSearchException e) {
			// NOAH: if logging, might be better to give class name and function
			// name, so developer could more easily track down source of
			// problem.
			LOGGER.warn("MessageSearchException in EntriesBetween Stat", e);
		} catch (NullPointerException e) {
			LOGGER.warn("NullPointerException thrown in numEntries()", e);
		}

		return result;
	}

	public JSONObject getStatisticsJSON() {
		JSONObject result = new JSONObject();
		long currTime = System.currentTimeMillis();
		Date currDate = new Date(currTime);
		Date prevDate = new Date(currTime - THIRTY_DAYS);

		// create value strings for current date and 30-days previous
		String u = new StringBuilder(SIMPLE_DATE.format(currDate)).toString();
		String l = new StringBuilder(SIMPLE_DATE.format(prevDate)).toString();
		setUpper(u);
		setLower(l);

		try {
			result.put(STAT_TIME_STAMP, System.currentTimeMillis());
			result.put(STAT_NUM_ENTRIES, numEntries());
		} catch (JSONException e) {
			LOGGER.warn("JSONException in EntriesBetween", e);
		}

		return result;
	}

	public JSONObject getStatisticsJSON(Map<String, String> map) {
		// TODO: Implement
		return getStatisticsJSON();
	}

	// required for spring to work
	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}
}