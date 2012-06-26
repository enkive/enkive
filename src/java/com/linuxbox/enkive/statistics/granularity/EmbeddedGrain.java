package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class EmbeddedGrain extends AbstractGrain{
	public EmbeddedGrain(StatsClient client) {
		super(client);
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
		
		// 2. loop over paths
		for (List<String> dataPath : dataPaths) {
			int totalWeight = 0;
			DescriptiveStatistics statsMaker = new DescriptiveStatistics();
			Object dataVal = null;
			Map<String, Object> statConsolidatedData = new HashMap<String, Object>();
			//3. loop over stat consolidation methods
			for(KeyDef keyDef: keys){
				for(String method: keyDef.getMethods()){	
					if(keyDef.getMethods() != null){
						//4. loop over data for consolidation Method
						for (Map<String, Object> dataMap : serviceData){
							//5. get variable at the end of the path
							dataVal = getDataVal(dataMap, dataPath);
							double input = -1;
							if (dataVal != null) {
								//6. extract relevant data
								if(((Map<String, Object>)dataVal).get(method) != null){
									dataVal = ((Map<String, Object>)dataVal).get(method);
									input = statToDouble(dataVal);
								}
								if (input > -1) {
									statsMaker.addValue(input);
								}
	
								if (input >= 0) {
									Integer statWeight = (Integer) dataMap
											.get(GRAIN_WEIGHT);
									if (statWeight == null) {
										statWeight = 1;
									}
	
									if(method.equals(GRAIN_AVG)){
										input = input*statWeight;
									}
									
									totalWeight += statWeight;
									//7. add to stat maker if relevant
									statsMaker.addValue(input);
								}
								
								//8. store in map if relevant method
								if (method.equals(GRAIN_SUM)) {
									statConsolidatedData.put(method,
											injectType(dataVal, statsMaker.getSum()));
								}
								else if (method.equals(GRAIN_MAX)) {
									statConsolidatedData.put(method,
											injectType(dataVal, statsMaker.getMax()));
								}
								else if (method.equals(GRAIN_MIN)) {
									statConsolidatedData.put(method,
											injectType(dataVal, statsMaker.getMin()));
								}
								else if (method.equals(GRAIN_AVG)) {
									statConsolidatedData.put(
											method,
											injectType(dataVal, statsMaker.getSum()
													/ totalWeight));
								}
								else if (method.equals(GRAIN_STD_DEV)) {
									statConsolidatedData.put(
											method,
											injectType(dataVal,
													statsMaker.getStandardDeviation()));
								}
							}
						}
					}
				}
			}
			
			//9. store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
			System.out.println("consolData-afterStoreOnPath: "
					+ consolidatedData);
		}
		return consolidatedData;
	}
}
