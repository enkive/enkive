package com.linuxbox.enkive.statistics.services.retrieval;

import java.util.Map;

//TODO Refactor this doesn't neet the gatherername so long as tracked by index
public abstract class StatsFilter {
	public String gathererName;
	public Map<String, Object> keys = null;
	public abstract Map<String, Object> getFilter();
}
