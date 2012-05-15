package com.linuxbox.enkive.statistics.storage.mongodb;

import java.util.Date;
import java.util.Map;

import com.linuxbox.enkive.statistics.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.storage.StatsStorageService;

public class MongoStatsStorageService implements StatsStorageService {

	/* NOAH: you'll need to fill this out */
	
	
	@Override
	public void storeStatistics(String service, Date timestamp,
			Map<String, String> data) throws StatsStorageException {
		// TODO Auto-generated method stub

	}

	@Override
	public void queryStatistics(String statsName, Date startingTimestamp,
			Date endingTimestamp) throws StatsStorageException {
		// TODO Auto-generated method stub

	}

}
