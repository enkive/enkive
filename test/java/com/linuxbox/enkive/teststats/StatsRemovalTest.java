package com.linuxbox.enkive.teststats;

import static org.junit.Assert.assertTrue;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.teststats.TestHelper;

public class StatsRemovalTest {
	private static StatsGathererService gatherTester;
	private static StatsClient client;

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		gatherTester = TestHelper.BuildGathererService();
		client = TestHelper.BuildClient();
	}

	@Test
	public void removeAllTest() throws ParseException, GathererException {
		List<RawStats> stats = gatherTester.gatherStats();
		Set<Map<String, Object>> statSet = new HashSet<Map<String, Object>>();
		for (RawStats rawStats : stats) {
			Map<String, Object> temp = rawStats.toMap();
			statSet.add(temp);
			if(temp.get(STAT_TIMESTAMP) instanceof Map){
				Map<String, Object> dateMap = new HashMap<String, Object>();
				dateMap.put(CONSOLIDATION_MIN, new Date(0L));
				dateMap.put(CONSOLIDATION_MAX, new Date(0L));
				temp.put(STAT_TIMESTAMP, dateMap);
			} else {
				temp.put(STAT_TIMESTAMP, new Date(0L));
			}
		}
		client.storeData(statSet);
		assertTrue("Error: the db is empty", !client.queryStatistics().isEmpty());
		Set<Map<String, Object>> dbStats = client.queryStatistics();
		Set<Object> ids = new HashSet<Object>();
		for (Map<String, Object> stat : dbStats) {
			ids.add(stat.get("_id"));
		}
		client.remove(ids);
		assertTrue("Error: the db is not empty", client.queryStatistics().isEmpty());
	}
}