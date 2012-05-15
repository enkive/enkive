package com.linuxbox.enkive.statistics.storage;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatsStorageService {
	void storeStatistics(String service, Date timestamp,
			Map<String, Object> data) throws StatsStorageException;

	List<Object> queryStatistics(String statsName, Date startingTimestamp,
			Date endingTimestamp) throws StatsStorageException;
}
