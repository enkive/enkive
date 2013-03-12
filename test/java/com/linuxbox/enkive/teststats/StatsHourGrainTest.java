/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TS_POINT;
import static com.linuxbox.enkive.statistics.VarsMaker.createListOfMaps;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.consolidation.HourConsolidator;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.mongodb.DBCollection;

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
			List<Map<String, Object>> statsToStore = createListOfMaps();
			if (i == 5) {
				cal.add(Calendar.HOUR, -1);
			}

			for (RawStats data : stats) {
				Map<String, Object> temp = data.toMap();
				Map<String, Object> date = (Map<String, Object>) temp
						.get(STAT_TIMESTAMP);
				date.put(CONSOLIDATION_MIN, cal.getTime());
				date.put(CONSOLIDATION_MAX, cal.getTime());
				date.put(STAT_TS_POINT, cal.getTime());
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
			List<List<Map<String, Object>>> data = grain.gathererFilter(name);
			List<Map<String, Object>> pData = data.get(0);
			int pSize = 0;
			if (pData != null) {
				pSize = pData.size();
			}
			List<Map<String, Object>> iData = data.get(1);
			int iSize = 0;
			if (iData != null) {
				iSize = iData.size();
			}
			assertTrue("incorrect number of objects for " + name + " : 5 vs. "
					+ iSize + " & " + pSize, iSize == 5 || pSize == 5);
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
		List<Map<String, Object>> consolidatedData = grain.consolidateData();
		assertTrue("the consolidated data is null", consolidatedData != null);
		String methods[] = { CONSOLIDATION_AVG, CONSOLIDATION_MAX,
				CONSOLIDATION_MIN };
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		statsMaker.addValue(111);
		statsMaker.addValue(11);
		statsMaker.addValue(1);
		Map<String, Object> statData = createMap();
		for (String method : methods) {
			grain.methodMapBuilder(method, statsMaker, statData);
		}
		assertTrue("methodMapBuilder returned null", statData != null);
	}

}
