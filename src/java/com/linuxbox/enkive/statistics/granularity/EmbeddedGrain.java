package com.linuxbox.enkive.statistics.granularity;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_IS_POINT;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class EmbeddedGrain extends AbstractGrain {
	public EmbeddedGrain(StatsClient client) {
		super(client);
	}

	@Override
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, ConsolidationKeyHandler keyDef,
			LinkedList<String> dataPath) {
		Map<String, Object> statConsolidatedData = new HashMap<String, Object>();
		int isPoint = (Integer)serviceData.iterator().next().get(STAT_IS_POINT);
		if (keyDef.getMethods() != null) {
			//loop over stat consolidation methods
			for (String method : keyDef.getMethods()) {
				DescriptiveStatistics statsMaker = new DescriptiveStatistics();
				Object dataVal = null;
				dataVal = null;
				//loop over data for consolidation Method
				LinkedList<String> tempPath = new LinkedList<String>(dataPath);
				if(isPointData(isPoint)){
					tempPath.add(method);
				} else {
					tempPath.add(GRAIN_SUM);
				}
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
				methodMapBuilder(method, statsMaker,
						statConsolidatedData);
			}

			// store stat methods' data on main consolidated map
			putOnPath(dataPath, consolidatedData, statConsolidatedData);
		}
	}
}
