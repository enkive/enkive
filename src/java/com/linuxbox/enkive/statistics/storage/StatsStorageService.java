package com.linuxbox.enkive.statistics.storage;

import java.util.Date;
import java.util.Map;

public interface StatsStorageService {
	void storeStatistics(String service, Date timestamp,
			Map<String, String> data) throws StatsStorageException;

	void queryStatistics(String statsName, Date startingTimestamp,
			Date endingTimestamp) throws StatsStorageException;
}
