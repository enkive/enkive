package com.linuxbox.enkive.statistics.services.retrieval;

import java.util.Date;
import java.util.Map;

/**
 * Describes a query for the stats database. If specified, will return entries
 * at or later than the start time stamp and less than (not less than or equal
 * to) the end time stamp. If either time stamp is null, then the query will not
 * consider it.
 * 
 * @author eric
 * 
 */
public abstract class StatsQuery {
	public Integer grainType;
	public Date startTimestamp = null;
	public Date endTimestamp = null;

	public abstract Map<String, Object> getQuery();
}
