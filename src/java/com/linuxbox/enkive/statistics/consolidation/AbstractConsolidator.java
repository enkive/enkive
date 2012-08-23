package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_SUM;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;
import com.linuxbox.enkive.statistics.services.retrieval.StatsTypeFilter;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsQuery;

@SuppressWarnings("unchecked")
public abstract class AbstractConsolidator implements Consolidator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Date endDate;
	protected Integer filterType;
	protected int grainType;
	protected boolean isEmbedded;
	protected Date startDate;

	public AbstractConsolidator(StatsClient client) {
		this.client = client;
		setDates();
		setTypes();
	}

	public void storeConsolidatedData() {
		client.storeData(consolidateData());
	}

	/**
	 * Builds a map that cooresponds to the consolidation methods
	 * @param method - the method to use
	 * @param exampleData - an example data object (for type consistancy after consolidation)
	 * @param statsMaker - the pre-populated DescriptiveStatstistics object to pull stats from
	 * @param statData - the map to populate with consolidated data
	 */
	public void methodMapBuilder(String method, DescriptiveStatistics statsMaker,
			Map<String, Object> statData) {
		if (method.equals(CONSOLIDATION_SUM)) {
			statData.put(method, statsMaker.getSum());
		} else if (method.equals(CONSOLIDATION_MAX)) {
			statData.put(method, statsMaker.getMax());
		} else if (method.equals(CONSOLIDATION_MIN)) {
			statData.put(method, statsMaker.getMin());
		} else if (method.equals(CONSOLIDATION_AVG)) {
			statData.put(method, statsMaker.getMean());
		} 
	}
	
	@Override
	public Set<Map<String, Object>> consolidateData() {
		Set<Map<String, Object>> storageData = new HashSet<Map<String, Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			List<Set<Map<String, Object>>> serviceData = gathererFilter(name);			
			if (!serviceData.isEmpty()) {
				Map<String, Object> mapToStore = new HashMap<String,Object>();
				
				//Interval
				Set<Map<String, Object>> intervalData = serviceData.get(0);
				if(intervalData != null){
					System.out.println("intervalData: " + intervalData);
/*					System.out.println("interval");
					System.out.println("next(): " + intervalData.iterator().next());
					System.out.println("new: " + new HashMap<String, Object>((Map<String,Object>)intervalData.iterator().next()));
*/					Map<String, Object> intervalTemplate = new HashMap<String, Object>((Map<String,Object>)intervalData.iterator().next());
					Map<String, Object> intervalMapToStore = new HashMap<String, Object>(intervalTemplate);
					
					List<ConsolidationKeyHandler> intervalKeys = new LinkedList<ConsolidationKeyHandler>(); 
					for(ConsolidationKeyHandler keyDef: attribute.getKeys()){
						if(!keyDef.isPoint()){
							intervalKeys.add(keyDef);
						}
					}
					
					generateConsolidatedMap(intervalTemplate, intervalMapToStore,
							new LinkedList<String>(), intervalKeys,
							intervalData);//going to need string for point/interval
					mapToStore.put(STAT_INTERVAL, intervalMapToStore);
				}
				
				//Point
				Set<Map<String, Object>> pointData = serviceData.get(1);
				if(pointData != null){
					System.out.println("pointData: " + pointData);
/*					System.out.println("points");
					System.out.println("next(): " + pointData.iterator().next());
					System.out.println("new: " + new HashMap<String, Object>((Map<String,Object>)pointData.iterator().next()));
*/					Map<String, Object> pointTemplate = new HashMap<String, Object>((Map<String,Object>)pointData.iterator().next());
					System.out.println("pointTemplate: " + pointTemplate);
					Map<String, Object> pointMapToStore = new HashMap<String, Object>(pointTemplate);
					
					List<ConsolidationKeyHandler> pointKeys = new LinkedList<ConsolidationKeyHandler>(); 
					for(ConsolidationKeyHandler keyDef: attribute.getKeys()){
						if(keyDef.isPoint()){
							pointKeys.add(keyDef);
						}
					}
					
					generateConsolidatedMap(pointTemplate, pointMapToStore,
							new LinkedList<String>(), pointKeys,
							pointData);
					mapToStore.put(STAT_POINT, pointMapToStore);
				}
				
				mapToStore.put(CONSOLIDATION_TYPE, grainType);
				
				Map<String, Object> dateMap = new HashMap<String, Object>();
				dateMap.put(CONSOLIDATION_MIN, startDate);
				dateMap.put(CONSOLIDATION_MAX, endDate);
				
				mapToStore.put(STAT_TIMESTAMP, dateMap);
				
				System.out.println("MapTOSTORE: " + mapToStore);
				
/*				Map<String, Object> example = new HashMap<String, Object>(
						serviceData.iterator().next());
				Map<String, Object> mapToStore = new HashMap<String, Object>(
						example);
				generateConsolidatedMap(example, mapToStore,
						new LinkedList<String>(), attribute.getKeys(),
						serviceData);//going to need string for point/interval
				mapToStore.put(CONSOLIDATION_TYPE, grainType);
				Map<String, Object> dateMap = new HashMap<String, Object>();
				dateMap.put(CONSOLIDATION_MIN, startDate);
				dateMap.put(CONSOLIDATION_MAX, endDate);
				mapToStore.put(STAT_TIMESTAMP, dateMap);
				if (mapToStore.containsKey("_id")) {
					mapToStore.remove("_id");
				}

				storageData.add(mapToStore);
*/			}
		}
		return storageData;
	}

	/** this method recurses through a given template map to add consolidated data to a new map
	 * as defined by each key's ConsolidationDefinition
	 * @param templateData - the map used to trace
	 * @param consolidatedMap - the map being built
	 * @param path - the path to variables being used for trace
	 * @param statKeys - the list of a gatherer's consolidation definitions
	 * @param gathererData- all the data cooresponding to a given gatherer
	 * @return returns the built consolidatedMap variable
	 */
	protected Map<String, Object> generateConsolidatedMap(
			Map<String, Object> templateData,
			Map<String, Object> consolidatedMap, LinkedList<String> path,
			List<ConsolidationKeyHandler> statKeys, Set<Map<String, Object>> gathererData) {
		for (String key : templateData.keySet()) {
			path.addLast(key);
			ConsolidationKeyHandler matchingDef = findMatchingPath(path, statKeys);
			if (matchingDef != null) {
				consolidateMaps(consolidatedMap, gathererData, matchingDef,
						path);
			} else {
				if (templateData.get(key) instanceof Map) {
					generateConsolidatedMap(
							(Map<String, Object>) templateData.get(key),
							consolidatedMap, path, statKeys, gathererData);
				}
			}
			path.removeLast();

		}
		return consolidatedMap;
	}

	/**
	 * this method takes a map and a path and uses the path to trace down the map to a data object
	 * it then returns that data
	 * @param dataMap - the map the data will be extracted from
	 * @param path - the path to traced on
	 * @return the found data object or null if path does not work
	 */
	protected Object getDataVal(Map<String, Object> dataMap, List<String> path) {
		Object map = dataMap;
		for (String key : path) {
			if (((Map<String, Object>) map).containsKey(key)) {
				if (((Map<String, Object>) map).get(key) instanceof Map) {
					map = ((Map<String, Object>) map).get(key);
				} else {
					return ((Map<String, Object>) map).get(key);
				}
			}
		}
		return null;
	}

	/** determines if a path matches any of the ConsolidationDefinitions for a given gatherer
	 * it does this by comparing each of the path's strings to each of the definition's strings
	 * asterisks are considered 'any' and are skipped
	 * @param path - the path to be checked
	 * @param keys - the gatherer's consolidation definitions
	 * @return if it finds a matching path it returns the corresponding ConsolidationDefinition
	 * if not it returns null
	 */
	private ConsolidationKeyHandler findMatchingPath(List<String> path, List<ConsolidationKeyHandler> keys) {
		for (ConsolidationKeyHandler def : keys) {// get one key definition
			if (def.getMethods() == null) {
				continue;
			}
			boolean isMatch = true;
			int pathIndex = 0;
			int defIndex = 0;
			String keyStr;
			String pathStr;
			List<String> keyString = def.getKey();
			if (keyString.size() > path.size()) {
				isMatch = false;
				continue;
			}

			while (pathIndex < path.size()) {
				if (defIndex >= keyString.size()) {
					isMatch = false;
					break;
				}

				while (keyString.get(defIndex).equals("*")
						&& defIndex < keyString.size()) {
					if (defIndex == keyString.size() - 1) {
						if (keyString.get(defIndex).equals("*")) {
							if (defIndex == path.size() - 1) {
								return def;
							} else {
								isMatch = false;
								break;
							}
						}
					} else {
						defIndex++;
						pathIndex++;
						keyStr = keyString.get(defIndex);
						pathStr = path.get(pathIndex);
					}
				}
				if (pathIndex >= path.size()) {
					isMatch = false;
					break;
				}

				keyStr = keyString.get(defIndex);
				pathStr = path.get(pathIndex);

				if (keyStr.equals(pathStr)) {
					pathIndex++;
					defIndex++;
				} else {
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				return def;
			}
		}
		return null;
	}

	/** this method takes a data object and inserts it at the end of a path on a given map
	 * @param path - the path to traverse
	 * @param statsData - the map to insert data into
	 * @param dataToAdd - the data to insert
	 */
	protected void putOnPath(List<String> path, Map<String, Object> statsData,
			Map<String, Object> dataToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, dataToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				} else {
					// TODO create the missing intervening maps
					LOGGER.error("Path does not exist");
					break;
				}
			}
			index++;
		}
	}
	
	private Set<Map<String, Object>> getStatTypeData(String gathererName, String type){
		StatsQuery intervalQuery = new MongoStatsQuery(gathererName, filterType, type, startDate, endDate);
		StatsFilter intervalFilter = new StatsTypeFilter(type);
		Set<Map<String, Object>> result = new HashSet<Map<String,Object>>();
		for(Map<String, Object> statsMap: client.queryStatistics(intervalQuery, intervalFilter)){
			statsMap.remove("_id");//TODO mongo specific pollution
			
			if(statsMap != null && !statsMap.isEmpty()){
//TODO				System.out.println("result: " + result);
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

	/**
	 * converts a statistic object into a double
	 * @param stat the statistic object to convert
	 * @return a double representing the statistic object
	 */
	protected double statToDouble(Object stat) {
		double input = -1;
		if (stat instanceof Integer) {
			input = (double) ((Integer) stat).intValue();
		} else if (stat instanceof Long) {
			input = (double) ((Long) stat).longValue();
		} else if (stat instanceof Double) {
			input = ((Double) stat).doubleValue();
		} else if (stat instanceof Date) {
			input = (double) ((Long) ((Date) stat).getTime()).longValue();
		} else {
			LOGGER.warn("statToDouble(Object stat)-unexpected Object type");
		}
		return input;
	}

	public abstract void setDates();
	
	public void setDates(Date upperDate, Date lowerDate){
		this.startDate = lowerDate;
		this.endDate = upperDate;
	}
	
	public abstract void setTypes();
	
	public void setTypes(int grainType, Integer filterType){
		this.grainType = grainType;
		this.filterType = filterType;
	}

	/**
	 * This method gets consolidated data from the service data and inserts it into
	 * the consolidateData map argument.
	 * 
	 * @param consolidatedData map to insert consolidated Data into (must have valid dataPath)
	 * if the path has data at the end it will be overwritten
	 * @param serviceData - all data relating to a service
	 * @param keyDef - defines the consolidation methods to use on the serviceData
	 * @param dataPath - the path on which to store the data in the consolidatedMap
	 */
	protected abstract void consolidateMaps(
			Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, ConsolidationKeyHandler keyDef,
			LinkedList<String> dataPath);
}
