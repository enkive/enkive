package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class HourGrain extends AbstractGrain {
	
	public HourGrain(StatsClient client){
		super(client);
	}	
	
	protected void setTypes(){
		grainType = GRAIN_HOUR;
		filterType = null;
	}
	
	public void setDates(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		endDate = cal.getTime();	
		cal.add(Calendar.HOUR_OF_DAY, -1);
		startDate = cal.getTime();
	}
	
	@SuppressWarnings("unchecked")
	protected Map<String, Object> consolidateMaps(
			Set<Map<String, Object>> serviceData, List<KeyDef> keys) {
		// Map<String, Object> result = consolidateMapHelper(map, path, keys,
		// new HashMap<String, Object>());
		// System.out.println("consolidateMap()-result: " + result);
		
		Map<String, Object> exampleData = (Map<String, Object>) serviceData
				.toArray()[0];

		// 1. recurse to find set of paths
		Set<List<String>> dataPaths = findPathSet(exampleData,
				new LinkedList<String>(), keys, new HashSet<List<String>>());
		Map<String, Object> consolidatedData = new HashMap<String, Object>(
				exampleData);
		// 2. TODO: check for pre-consolidated data
		// know from that fact if it is hourly, daily, etc.
		
		// 3. loop over paths
		for (List<String> dataPath : dataPaths) {
			int totalWeight = 0;
			DescriptiveStatistics statsMaker = new DescriptiveStatistics();
			DescriptiveStatistics avgStatsMaker = new DescriptiveStatistics();
			Object dataVal = null;

			// 4. get data from maps and add to statMakers
			for (Map<String, Object> dataMap : serviceData) {
				dataVal = getDataVal(dataMap, dataPath);
//				System.out.println("maps()-dataVal: " + dataVal);
				double input = -1;

				if (dataVal != null) {
					input = statToDouble(dataVal);
					if (input > -1) {
						statsMaker.addValue(input);
					}

					if (input >= 0) {
						Integer statWeight = (Integer) dataMap
								.get(GRAIN_WEIGHT);
						if (statWeight == null) {
							statWeight = 1;
						}

						totalWeight += statWeight;
						avgStatsMaker.addValue(input * statWeight);
						statsMaker.addValue(input);
					}
				}
			}

			// 5. loop over methods to populate map with max, min, etc.
			Map<String, Object> methodData = new HashMap<String, Object>();
			for (KeyDef keyDef : keys) {
				if (keyDef.getMethods() != null) {
					for (String method : keyDef.getMethods()) {
						if (method.equals(GRAIN_SUM)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getSum()));
						}
						if (method.equals(GRAIN_MAX)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getMax()));
						}
						if (method.equals(GRAIN_MIN)) {
							methodData.put(method,
									injectType(dataVal, statsMaker.getMin()));
						}
						if (method.equals(GRAIN_AVG)) {
							methodData.put(
									method,
									injectType(dataVal, avgStatsMaker.getSum()
											/ totalWeight));
						}
						if (method.equals(GRAIN_STD_DEV)) {
							methodData.put(
									method,
									injectType(dataVal,
											statsMaker.getStandardDeviation()));
						}
					}
				}
			}
			// 6. store in new map on path
//			System.out.println("consolData-beforeStoreOnPath: "
//					+ consolidatedData);
			putOnPath(dataPath, consolidatedData, methodData);
//			System.out.println("consolData-afterStoreOnPath: "
//					+ consolidatedData);
		}
		return consolidatedData;
	}
}
