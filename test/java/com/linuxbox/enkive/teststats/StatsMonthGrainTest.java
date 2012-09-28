package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.consolidation.DayConsolidator;
import com.linuxbox.enkive.statistics.consolidation.MonthConsolidator;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;
import com.mongodb.DBCollection;

public class StatsMonthGrainTest {
	private static StatsClient client;
	private static MonthConsolidator grain;
	private static DBCollection coll;
	private static long dataCount;

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		coll = TestHelper.GetTestCollection();
		client = TestHelper.BuildClient();
		grain = new MonthConsolidator(client);

		// clean up if week was run...
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put(CONSOLIDATION_TYPE, CONSOLIDATION_DAY);
		StatsQuery statsQuery = new MongoStatsQuery(null, CONSOLIDATION_DAY, null);
		Set<Object> ids = new HashSet<Object>();
		for (Map<String, Object> mapToDelete : client.queryStatistics(statsQuery)) {
			ids.add(mapToDelete.get("_id"));
		}

		if (!ids.isEmpty()) {
			client.remove(ids);
		}

		// TODO
		List<Map<String, Object>> stats = (new DayConsolidator(client))
				.consolidateData();
		Map<String, Object> timeMap = new HashMap<String, Object>();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		for (int i = 0; i < 10; i++) {
			if (i == 5) {
				cal.add(Calendar.MONTH, -1);
			}
			timeMap.put(CONSOLIDATION_MAX, cal.getTime());
			timeMap.put(CONSOLIDATION_MIN, cal.getTime());
			for (Map<String, Object> data : stats) {
				data.put(STAT_TIMESTAMP, timeMap);
			}
			client.storeData(stats);
		}
		dataCount = coll.count();
	}

	@Test
	public void correctQueryTest() {
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			List<Map<String, Object>> data = grain.gathererFilter(name).get(0);
			int size = 0;
			if(data != null){
				size = data.size();
			}
			assertTrue(
					"the query did not return the correct number of objects: 5 vs. "
							+ size, size == 5);
		}
	}

	@Test
	public void noDeletedDataTest() {
		long count = coll.count();
		assertTrue("data was deleted by consolidatation: dataCount before: "
				+ dataCount + "dataCount now: " + count, count >= dataCount);
	}

	@Test
	public void consolidationMethods() {
		List<Map<String, Object>> consolidatedData = grain.consolidateData();
		assertTrue("the consolidated data is null", consolidatedData != null);
		String methods[] = { CONSOLIDATION_AVG, CONSOLIDATION_MAX, CONSOLIDATION_MIN };
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		statsMaker.addValue(111);
		statsMaker.addValue(11);
		statsMaker.addValue(1);
		Map<String, Object> statData = new HashMap<String, Object>();
		for (String method : methods) {
			grain.methodMapBuilder(method, statsMaker, statData);
		}
		assertTrue("methodMapBuilder returned null", statData != null);
	}
}
