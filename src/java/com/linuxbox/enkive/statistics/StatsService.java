package com.linuxbox.enkive.statistics;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;


public interface StatsService {

	public JSONObject getStatisticsJSON() throws JSONException;
	public JSONObject getStatisticsJSON(Map<String, String> args) throws JSONException;
	
}
