package com.linuxbox.enkive.statistics.services.retrieval.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

import java.util.Date;
import java.util.Map;

import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Describes a query for the stats database. If specified, will return entries
 * at or later than the start time stamp and less than (not less than or equal
 * to) the end time stamp. If either time stamp is null, then the query will not
 * consider it.
 * 
 * @author eric
 * 
 */
public class MongoStatsDateQuery extends StatsQuery{
	public MongoStatsDateQuery(Date startTimestamp, Date endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
	
	public Map<String, Object> getQuery() {
		Map<String, Object> mongoQuery = new BasicDBObject();
		DBObject time = new BasicDBObject();
		String tsStartKey;
		String tsEndKey;
		tsStartKey = STAT_TIMESTAMP + "." + GRAIN_MIN;
		tsEndKey   = STAT_TIMESTAMP + "." + GRAIN_MAX;
		
		if(startTimestamp != null){
			time = new BasicDBObject();
			time.put("$gte", startTimestamp);
			mongoQuery.put(tsStartKey, time);
		}
		if(endTimestamp != null){
			time = new BasicDBObject();
			time.put("$lt", endTimestamp);
			mongoQuery.put(tsEndKey, time);	
		}
		
		if(grainType != null){
			if(grainType == 0){
				mongoQuery.put(GRAIN_TYPE, null);
			} else {
				mongoQuery.put(GRAIN_TYPE, grainType);
			}
		}
		
		return mongoQuery;
	}
}
