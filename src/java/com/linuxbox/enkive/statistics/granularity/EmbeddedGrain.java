package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.HashMap;
import java.util.LinkedList;
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
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, KeyDef keyDef,
			LinkedList<String> dataPath) {
		Map<String, Object> statConsolidatedData = new HashMap<String, Object>();

		// 2. loop over paths
		if (keyDef.getMethods() != null) {
			// 3. loop over stat consolidation methods
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
				methodMapBuilder(method, dataVal, statsMaker, statConsolidatedData, totalWeight);
			}

			// 9. store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
		}
	}
	
	@Override
	protected Set<Map<String, Object>> serviceFilter(String name) {
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		Map<String, Object> time = new HashMap<String, Object>();
		time.put("$gte", startDate.getTime());
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MIN, time);
		time = new HashMap<String, Object>();
		time.put("$lt", endDate.getTime());
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MAX, time);
		keyVals.put(GRAIN_TYPE, filterType);

		query.putAll(keyVals);
		query.put(STAT_SERVICE_NAME, name);

		Set<Map<String, Object>> result = client.directQuery(query);
		return result;
	}
}
