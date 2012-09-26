package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.consolidation.HourConsolidator;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.mongodb.DBCollection;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

public class StatsHourGrainTest {
	private static StatsGathererService gatherTester;
	private static StatsClient client;
	private static HourConsolidator grain;
	private static DBCollection coll;
	private static long dataCount;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		gatherTester = TestHelper.BuildGathererService();
		coll = TestHelper.GetTestCollection();
		client = TestHelper.BuildClient();
		grain = new HourConsolidator(client);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		for (int i = 0; i < 10; i++) {
			List<RawStats> stats = gatherTester.gatherStats();
			Set<Map<String,Object>> statsToStore = new HashSet<Map<String,Object>>();
			if (i == 5) {
				cal.add(Calendar.HOUR, -1);
			}
			System.out.println("i: " + i + " cal.time: " + cal.getTime());
			
			for (RawStats data : stats) {
				Map<String, Object> temp = data.toMap();
				Map<String, Object> date = (Map<String,Object>)temp.get(STAT_TIMESTAMP);
				date.put(CONSOLIDATION_MIN, cal.getTime());
				date.put(CONSOLIDATION_MAX, cal.getTime());
				statsToStore.add(temp);
			}
			client.storeData(statsToStore);
		}
		dataCount = coll.count();
	}

	@Test
	public void correctQueryTest() {
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			int size = grain.gathererFilter(name).size();
			grain.consolidateData();
			assertTrue("incorrect number of objects for " + name + " : 5 vs. " + size, size == 5);
		}
	}

	@Test
	public void noDeletedDataTest() {
		long count = coll.count();
		assertTrue("data was deleted by consolidatation: dataCount before: "
				+ dataCount + " dataCount now: " + count, count >= dataCount);
	}

	@Test
	public void consolidationMethods() {
		Set<Map<String, Object>> consolidatedData = grain.consolidateData();
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
