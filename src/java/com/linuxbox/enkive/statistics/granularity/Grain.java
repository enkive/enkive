package com.linuxbox.enkive.statistics.granularity;

import java.util.Map;
import java.util.Set;

public interface Grain {
	public Set<Map<String, Object>> consolidateData();
	public void storeConsolidatedData();
	public Set<Map<String, Object>> serviceFilter(String name);
}
