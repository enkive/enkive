package com.linuxbox.enkive.statistics.services.retrieval;

import java.util.Map;

//TODO Refactor this doesn't neet the gatherername so long as tracked by intex
public abstract class StatsFilter {
	public abstract Map<String, Object> getFilter();
}
