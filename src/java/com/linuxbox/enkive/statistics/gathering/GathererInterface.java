package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.StatsStorageService;

public interface GathererInterface {
	
	public void setStorageService(StatsStorageService storageService);
	
	public void setSchedule(String schedule);
	
	public Map<String, Object> getStatistics() throws GathererException;

	public Map<String, Object> getStatistics(String[] keys)
			throws GathererException;
}
