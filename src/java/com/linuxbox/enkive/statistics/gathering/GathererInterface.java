package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public interface GathererInterface {
	public void storeStats() throws StatsStorageException;

	public void setStorageService(StatsStorageService storageService);
	
	public Map<String, Object> getStatistics() throws GathererException;

	public Map<String, Object> getStatistics(String[] keys)
			throws GathererException;
	
	public GathererAttributes getAttributes();
}
