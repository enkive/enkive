package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.granularity.HourGrain;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.mongodb.DBCollection;

public class StatsHourGrainTest {
	private static StatsGathererService gatherTester;
	private static StatsClient client;
	private static HourGrain grain;
	private static DBCollection coll;
	private static long dataCount;

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {

		gatherTester = TestHelper.BuildGathererService();
		coll = TestHelper.GetTestCollection();
		client = TestHelper.BuildClient();
		
		grain = new HourGrain(client);

		for (int i = 0; i < 10; i++) {
			List<RawStats> stats = gatherTester.gatherStats();
			Calendar cal = Calendar.getInstance();
			if (i < 5) {
				cal.add(Calendar.HOUR, -1);
			}
			for (RawStats data : stats) {
				data.setTimestamp(cal.getTime());
			}
			client.storeRawStatsData(stats);
		}
		dataCount = coll.count();
	}

	@Test
	public void correctQueryTest() {
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			int size = grain.gathererFilter(name).size();
			grain.consolidateData();
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
		Set<Map<String, Object>> consolidatedData = grain.consolidateData();
		assertTrue("the consolidated data is null", consolidatedData != null);
		String methods[] = { GRAIN_AVG, GRAIN_MAX, GRAIN_MIN };
		Object exampleData = new Integer(10);
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		statsMaker.addValue(111);
		statsMaker.addValue(11);
		statsMaker.addValue(1);
		Map<String, Object> statData = new HashMap<String, Object>();
		for (String method : methods) {
			grain.methodMapBuilder(method, exampleData, statsMaker, statData);
		}
		assertTrue("methodMapBuilder returned null", statData != null);
	}
}
