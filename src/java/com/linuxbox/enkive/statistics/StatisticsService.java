package com.linuxbox.enkive.statistics;

import org.json.JSONException;
import org.json.JSONObject;

public interface StatisticsService {

	public JSONObject getStatisticsJSON() throws JSONException;
	
}
