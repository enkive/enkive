package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class EmbeddedGrain extends AbstractGrain {
	public EmbeddedGrain(StatsClient client) {
		super(client);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map<String, Object> consolidateMaps(
			Set<Map<String, Object>> serviceData, List<KeyDef> keys) {
		Map<String, Object> exampleData = (Map<String, Object>) serviceData
				.toArray()[0];

		// 1. recurse to find set of paths
		Set<List<String>> dataPaths = findPathSet(exampleData,
				new LinkedList<String>(), keys, new HashSet<List<String>>());
		Map<String, Object> consolidatedData = new HashMap<String, Object>(
				exampleData);

		Map<String, Object> statConsolidatedData = new HashMap<String, Object>();

		// 2. loop over paths
		for (List<String> dataPath : dataPaths) {
			// 3. loop over stat consolidation methods
			for (KeyDef keyDef : keys) {
				if (keyDef.getMethods() != null) {
					for (String method : keyDef.getMethods()) {
						DescriptiveStatistics statsMaker = new DescriptiveStatistics();
						Object dataVal = null;
						int totalWeight = 0;
						dataVal = null;
						// 4. loop over data for consolidation Method
						LinkedList<String> tempPath = new LinkedList<String>(
								dataPath);
						tempPath.add(method);
						double input = -1;
						// int i = 0;
						for (Map<String, Object> dataMap : serviceData) {
							// 5. get variable at the end of the path
							input = -1;
							dataVal = getDataVal(dataMap, tempPath);
							if (dataVal != null) {
								// 6. extract relevant data
								input = statToDouble(dataVal);

								if (input > -1) {
									Integer statWeight = (Integer) dataMap
											.get(GRAIN_WEIGHT);
									if (statWeight == null) {
										statWeight = 1;
									}

									if (method.equals(GRAIN_AVG)) {
										input = input * statWeight;
									}

									totalWeight += statWeight;
									// 7. add to stat maker if relevant
									statsMaker.addValue(input);
								}
							}
						}
						// 8. store in map if relevant method
						if (method.equals(GRAIN_SUM)) {
							statConsolidatedData.put(method,
									injectType(dataVal, statsMaker.getSum()));
						} else if (method.equals(GRAIN_MAX)) {
							statConsolidatedData.put(method,
									injectType(dataVal, statsMaker.getMax()));
						} else if (method.equals(GRAIN_MIN)) {
							statConsolidatedData.put(method,
									injectType(dataVal, statsMaker.getMin()));
						} else if (method.equals(GRAIN_AVG)) {
							statConsolidatedData.put(
									method,
									injectType(dataVal, statsMaker.getSum()
											/ totalWeight));
						} else if (method.equals(GRAIN_STD_DEV)) {
							statConsolidatedData.put(
									method,
									injectType(dataVal,
											statsMaker.getStandardDeviation()));
						}
					}
				}
			}

			// 9. store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
		}
		System.out.println("consolidatedEmbeddedData: " + consolidatedData);
		return consolidatedData;
	}

	@Override
	protected Set<Map<String, Object>> serviceFilter(String name) {
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		Map<String, Object> time = new HashMap<String, Object>();
		// TODO FOR TESTING ONLY
		startDate = new Date(0L);
		endDate = new Date();
		time.put("$gte", startDate.getTime());
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MIN, time);
		time = new HashMap<String, Object>();
		time.put("$lt", endDate.getTime());
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MAX, time);
		keyVals.put(GRAIN_TYPE, filterType);

		query.putAll(keyVals);
		query.put(STAT_SERVICE_NAME, name);

		Set<Map<String, Object>> result = client.directQuery(query);
		// TODO System.out.println("serviceFilter-result: " + result);
		// System.out.println("serviceFilter-Query: " + query);
		return result;
	}
}
