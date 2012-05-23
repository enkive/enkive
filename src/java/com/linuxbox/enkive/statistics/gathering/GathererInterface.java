package com.linuxbox.enkive.statistics.gathering;

import java.util.Map;

public interface GathererInterface {
	public Map<String, Object> getStatistics() throws GathererException;

	public Map<String, Object> getStatistics(String[] keys)
			throws GathererException;
}
