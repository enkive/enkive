package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

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

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

@SuppressWarnings("unchecked")
public abstract class AbstractGrain implements Grain {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Date endDate;
	protected Integer filterType;
	protected int grainType;
	protected boolean isEmbedded;
	protected Date startDate;

	public AbstractGrain(StatsClient client) {
		this.client = client;
		setDates();
		setTypes();
	}
	
	public void storeConsolidatedData(){
		client.storeData(consolidateData());
	}

	public void methodMapBuilder(String method, Object exampleData, DescriptiveStatistics statsMaker, Map<String, Object> statData){
		if (method.equals(GRAIN_SUM)) {
			statData.put(method,
					injectType(exampleData, statsMaker.getSum()));
		} else if (method.equals(GRAIN_MAX)) {
			statData.put(method,
					injectType(exampleData, statsMaker.getMax()));
		} else if (method.equals(GRAIN_MIN)) {
			statData.put(method,
					injectType(exampleData, statsMaker.getMin()));
		} else if (method.equals(GRAIN_AVG)) {
			statData.put(
					method,
					injectType(exampleData, statsMaker.getMean()));
		} else if (method.equals(GRAIN_STD_DEV)) {
			statData.put(
					method,
					injectType(exampleData,
							statsMaker.getStandardDeviation()));
		}
	}
	
	@Override
	public Set<Map<String, Object>> consolidateData() {
		// build set for each service
		Set<Map<String, Object>> storageData = new HashSet<Map<String, Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			Set<Map<String, Object>> serviceData = serviceFilter(name);
			if (!serviceData.isEmpty()) {
				Map<String, Object> example = new HashMap<String, Object>(serviceData.iterator().next());
				Map<String, Object> mapToStore = new HashMap<String, Object>(example);
				generateConsolidatedMap(example, mapToStore,
						 new LinkedList<String>(), attribute.getKeys(), serviceData);
				mapToStore.put(GRAIN_TYPE, grainType);
				if (mapToStore.containsKey("_id")) {
					mapToStore.remove("_id");
				}
				storageData.add(mapToStore);
			}
		}
		return storageData;
	}

	protected abstract void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, KeyDef keyDef, LinkedList<String> dataPath);
	

	protected Map<String, Object> generateConsolidatedMap(Map<String, Object> templateData, Map<String,Object> consolidatedMap,
			LinkedList<String> path, List<KeyDef> statKeys,
			 Set<Map<String, Object>> serviceData) {
		//loop through a template map
		for (String key : templateData.keySet()) {
			path.addLast(key);
			KeyDef matchingKeyDef = findMatchingPath(path, statKeys);
			//if path matched
			if (matchingKeyDef != null) {
				//add that data to the consolidatedMap 
				consolidateMaps(consolidatedMap, serviceData, matchingKeyDef, path);
			//else recurse again
			} else {
				if (templateData.get(key) instanceof Map) {
					generateConsolidatedMap((Map<String, Object>) templateData.get(key), consolidatedMap,  path,
							statKeys, serviceData);
				}
			}
			path.removeLast();
		}
		return consolidatedMap;
	}

	protected int findWeight(Set<Map<String, Object>> serviceData) {
		return serviceData.size();
	}

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

	protected Object injectType(Object example, double value) {
		Object result = null;
		if (example instanceof Integer) {
			result = (int) value;
		} else if (example instanceof Long) {
			result = (long) value;
		} else if (example instanceof Double) {
			result = value;
		}

		return result;
	}
	
	private KeyDef findMatchingPath(List<String> path, List<KeyDef> keys) {
		for (KeyDef def : keys) {// get one key definition
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

			while (pathIndex < path.size()) {// run through it to compare to
												// path
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
				/*regexpression code 
				 * if(keyString.get(defIndex).equals("**") && defIndex <
				 * keyString.size()) { if(defIndex == keyString.size()-1){ //
				 * break; } defIndex++;
				 * 
				 * if (path.contains(keyString.get(defIndex))) { //jump to
				 * matching index for (; pathIndex < path.size(); pathIndex++) {
				 * if (path.get(pathIndex).equals(keyString.get(defIndex))) {
				 * break; } } } else { // return false; //
				 * isMatch = false; break; } }
				 */
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
			}// no matches found
		}
		return null;
	}

	 boolean pathMatches(List<String> path, List<KeyDef> keys) {
		final KeyDef def = findMatchingPath(path, keys);
		return def != null;
	}

	protected void putOnPath(List<String> path,
			Map<String, Object> statsData, Map<String, Object> dataToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, dataToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				} else {
					LOGGER.error("Cannot put data on path");
				}
			}
			index++;
		}
	}

	public Set<Map<String, Object>> serviceFilter(String name) {
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterType);
		query.put(name, keyVals);
		Set<Map<String, Object>> result = client.queryStatistics(query,
				startDate, endDate);
		return result;
	}

	protected abstract void setDates();

	protected abstract void setTypes();

	protected double statToDouble(Object stat) {
		double input = -1;
		if (stat instanceof Integer) {
			input = (double) ((Integer) stat).intValue();
		} else if (stat instanceof Long) {
			input = (double) ((Long) stat).longValue();
		} else if (stat instanceof Double) {
			input = ((Double) stat).doubleValue();
		} else {
			LOGGER.warn("statToDouble(Object stat)-unexpected Object type");
		}
		return input;
	}
}
