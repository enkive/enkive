package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_RAW;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_SUM;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.StatsTypeFilter;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import static com.linuxbox.enkive.statistics.VarsMaker.createSetOfMaps;
import static com.linuxbox.enkive.statistics.VarsMaker.createLinkedListOfStrs;
public class HourConsolidator extends AbstractConsolidator {
	public HourConsolidator(StatsClient client) {
		super(client);
	}

	@SuppressWarnings("unchecked")
	private Set<Map<String, Object>> getStatTypeData(String gathererName, String type){
		StatsQuery query = new MongoStatsQuery(gathererName, filterType, type, startDate, endDate);
		StatsFilter filter = new StatsTypeFilter(type);
		Set<Map<String, Object>> result = createSetOfMaps();
		Set<Map<String, Object>> queryData = client.queryStatistics(query, filter);
		
		for(Map<String, Object> statsMap: queryData){
			statsMap.remove("_id");//WARNING mongo specific pollution
			if(statsMap != null && !statsMap.isEmpty()){
				result.add((Map<String,Object>)statsMap.get(type));
			}
		}
		return result;
	}
	
	public List<Set<Map<String, Object>>> gathererFilter(String gathererName) {
		List<Set<Map<String,Object>>> result = new LinkedList<Set<Map<String,Object>>>();

		//interval stats
		Set<Map<String, Object>> intervalData = getStatTypeData(gathererName, STAT_INTERVAL);
		if(!intervalData.isEmpty()){
			result.add(intervalData);
		} else {
			result.add(null);
		}
		
		//point stats
		Set<Map<String, Object>> pointData = getStatTypeData(gathererName, STAT_POINT);
		if(!pointData.isEmpty()){
			result.add(pointData);
		} else {
			result.add(null);
		}
		return result;
	}
	
	@Override
	public Set<Map<String, Object>> consolidateData() {
		Set<Map<String, Object>> storageData = createSetOfMaps();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			List<Set<Map<String, Object>>> serviceData = gathererFilter(name);			
			if (!serviceData.isEmpty()) {
				Map<String, Object> mapToStore = createMap();
				
				//Interval
				Set<Map<String, Object>> intervalData = serviceData.get(0);
				if(intervalData != null){
					Map<String, Object> intervalTemplate = createMap((Map<String,Object>)intervalData.iterator().next());
					Map<String, Object> intervalMapToStore = createMap(intervalTemplate);
					
					List<ConsolidationKeyHandler> intervalKeys = new LinkedList<ConsolidationKeyHandler>(); 
					for(ConsolidationKeyHandler keyDef: attribute.getKeys()){
						if(!keyDef.isPoint()){
							intervalKeys.add(keyDef);
						}
					}
					
					generateConsolidatedMap(intervalTemplate, intervalMapToStore,
							createLinkedListOfStrs(), intervalKeys,
							intervalData);//going to need string for point/interval
					mapToStore.putAll(intervalMapToStore);
				}
				//Point
				Set<Map<String, Object>> pointData = serviceData.get(1);
				if(pointData != null){
					Map<String, Object> pointTemplate = createMap((Map<String,Object>)pointData.iterator().next());
					Map<String, Object> pointMapToStore = createMap(pointTemplate);
					
					List<ConsolidationKeyHandler> pointKeys = new LinkedList<ConsolidationKeyHandler>(); 
					for(ConsolidationKeyHandler keyDef: attribute.getKeys()){
						if(keyDef.isPoint()){
							pointKeys.add(keyDef);
						}
					}
					
					generateConsolidatedMap(pointTemplate, pointMapToStore,
							new LinkedList<String>(), pointKeys,
							pointData);
					mapToStore.putAll(pointMapToStore);
				}		
				
				if(!mapToStore.isEmpty()){
					Map<String, Object> dateMap = createMap();
					dateMap.put(CONSOLIDATION_MIN, startDate);
					dateMap.put(CONSOLIDATION_MAX, endDate);
					
					mapToStore.put(STAT_TIMESTAMP, dateMap);
					mapToStore.put(CONSOLIDATION_TYPE, consolidationType);
					mapToStore.put(STAT_GATHERER_NAME, attribute.getName());
					storageData.add(mapToStore);
				}
			}
		}
		return storageData;
	}
	
	@Override
	protected void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, ConsolidationKeyHandler keyDef,
			LinkedList<String> dataPath) {

		if (keyDef.getMethods() != null) {
			boolean isPoint = keyDef.isPoint();
			DescriptiveStatistics statsMaker = new DescriptiveStatistics();
			Object dataVal = null;

			// get data from maps and add to statMakers
			for (Map<String, Object> dataMap : serviceData) {
				dataVal = getDataVal(dataMap, dataPath);
				double input = -1;

				if (dataVal != null) {
					input = statToDouble(dataVal);
					if (input > -1) {
						statsMaker.addValue(input);
					}
				}
			}

			// loop over methods to populate map with max, min, etc.
			Map<String, Object> methodData = createMap();
			for (String method : keyDef.getMethods()) {
				if(!method.equals(CONSOLIDATION_SUM)){//may not be user defined
					methodMapBuilder(method, statsMaker, methodData);
				}
			}
			if(!isPoint){//create sum
				methodMapBuilder(CONSOLIDATION_SUM, statsMaker, methodData);
			}
			// store in new map on path
			putOnPath(dataPath, consolidatedData, methodData);
		}
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		Date upperDate = cal.getTime();
		cal.add(Calendar.HOUR_OF_DAY, -1);
		Date lowerDate = cal.getTime();	
		setDates(upperDate, lowerDate);
	}

	@Override
	public void setTypes() {
		setTypes(CONSOLIDATION_HOUR, CONSOLIDATION_RAW);
	}
}
