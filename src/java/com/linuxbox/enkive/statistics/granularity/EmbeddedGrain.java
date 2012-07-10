package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class EmbeddedGrain extends AbstractGrain {
	public EmbeddedGrain(StatsClient client) {
		super(client);
	}

	@Override
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, KeyConsolidationHandler keyDef,
			LinkedList<String> dataPath) {
		Map<String, Object> statConsolidatedData = new HashMap<String, Object>();
		if (keyDef.getMethods() != null) {
			//loop over stat consolidation methods
			for (String method : keyDef.getMethods()) {
				DescriptiveStatistics statsMaker = new DescriptiveStatistics();
				Object dataVal = null;
				dataVal = null;
				//loop over data for consolidation Method
				LinkedList<String> tempPath = new LinkedList<String>(dataPath);
				tempPath.add(method);
				double input = -1;
				for (Map<String, Object> dataMap : serviceData) {
					//go to end of path & get variable
					input = -1;
					dataVal = getDataVal(dataMap, tempPath);
					if (dataVal != null) {
						//extract relevant data from end of path
						input = statToDouble(dataVal);

						if (input > -1) {
							//add to stat maker if relevant
							statsMaker.addValue(input);
						}
					}
				}
				//store in map if method is valid
				methodMapBuilder(method, dataVal, statsMaker,
						statConsolidatedData);
			}

			// store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
		}
	}

	@Override
	public Set<Map<String, Object>> gathererFilter(String name) {
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		Map<String, Object> time = new HashMap<String, Object>();
		time.put("$gte", startDate);
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MIN, time);
		time = new HashMap<String, Object>();
		time.put("$lt", endDate);
		keyVals.put(STAT_TIME_STAMP + "." + GRAIN_MAX, time);
		keyVals.put(GRAIN_TYPE, filterType);

		query.putAll(keyVals);
		query.put(STAT_SERVICE_NAME, name);
		Set<Map<String, Object>> result = client.directQuery(query);
		return result;
	}
}
