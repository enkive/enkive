package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

public class GathererAttributes {
	private int interval;//minutes
	private START start;
	private Map<String, Object> defaultMap;
	
	public enum START{
		ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE,
		ONEP, TWOP, THREEP, FOURP, FIVEP, SIXP, SEVENP, EIGHTP, NINEP, TENP, ELEVENP, TWELVEP
	}
	
	public GathererAttributes(int interval, START start, Map<String, Object> map) {
		this.interval = interval;//fraction of hour
		this.start = start;
		defaultMap = map;
	}

	public Map<String, Object> getDefaultMap() {
		return defaultMap;
	}

}
