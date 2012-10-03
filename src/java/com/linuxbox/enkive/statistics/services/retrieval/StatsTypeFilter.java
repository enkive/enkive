package com.linuxbox.enkive.statistics.services.retrieval;

import java.util.HashMap;
import java.util.Map;

public class StatsTypeFilter extends StatsFilter {
	Map<String, Object> filter = new HashMap<String, Object>();

	public StatsTypeFilter(String type) {
		this.filter.put(type, 1);
	}

	public Map<String, Object> getFilter() {
		return filter;
	}
}
