package com.linuxbox.enkive.teststats;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.GathererException;
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
		for (RawStats rawStats : stats) {
			rawStats.setTimestamp(new Date(0L));
		}
		client.storeRawStatsData(stats);
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