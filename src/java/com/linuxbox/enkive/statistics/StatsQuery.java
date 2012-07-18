package com.linuxbox.enkive.statistics;

import java.util.Date;

/**
 * Describes a query for the stats database. If specified, will return entries
 * at or later than the start time stamp and less than (not less than or equal
 * to) the end time stamp. If either time stamp is null, then the query will not
 * consider it.
 * 
 * @author eric
 * 
 */
public class StatsQuery {
	public String gathererName;
	public Integer grainType;
	public Date startTimestamp = null;
	public Date endTimestamp = null;

	public StatsQuery(Date startTimestamp, Date endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
	
	public StatsQuery(String gathererName, Integer grainType) {
		this.gathererName = gathererName;
		this.grainType = grainType;
	}

	public StatsQuery(String gathererName, Integer grainType,
			Date startTimestamp, Date endTimestamp) {
		this(gathererName, grainType);
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
}
