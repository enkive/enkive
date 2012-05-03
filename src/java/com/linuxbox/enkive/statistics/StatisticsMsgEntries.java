package com.linuxbox.enkive.statistics;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public class StatisticsMsgEntries implements StatisticsService {
	MessageSearchService searchService;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	
	protected String lower, upper;
	//for testing "2001-11-18", "2001-11-18"
	public StatisticsMsgEntries(String l, String u) {
		lower = l;
		upper = u;
	}
	
	public int numEntries(){
		HashMap<String, String> hmap = new HashMap<String, String>();
		hmap.put("dateEarliest", lower);
		hmap.put("dateLatest", upper);
		int result = -1;//-1 is error flag
		try {
			result = searchService.search(hmap).getMessageIds().size();
		} catch (MessageSearchException e) {
			LOGGER.warn("MessageSearchException in EntriesBetween Stat", e);
		}
		return result;
	}

	public JSONObject getStatisticsJSON(){
		JSONObject result = new JSONObject();
		try {
			result.put("NumOfEntries", numEntries());
		} catch (JSONException e) {
			LOGGER.warn("JSONException in EntriesBetween", e);
		}
		return result;
	}

	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService =  searchService;
	}
}