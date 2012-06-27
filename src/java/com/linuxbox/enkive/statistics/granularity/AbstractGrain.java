package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEIGHT;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		LOGGER.info("starting abstract client");
		setDates();
		setTypes();
		consolidateData();
		LOGGER.info("finishing abstract client");
	}

	@Override
	public void consolidateData() {
		// build set for each service
		Set<Map<String, Object>> storageData = new HashSet<Map<String, Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			Set<Map<String, Object>> serviceData = serviceFilter(name);
			if (!serviceData.isEmpty()) {
				consolidateMaps(serviceData, attribute.getKeys());
				storageData = new HashSet<Map<String, Object>>();
				Map<String, Object> mapToStore =  generateConsolidatedMap(templateData, Map<String,Object> consolidatedMap,
						LinkedList<String> path, List<KeyDef> statKeys,
						 Set<Map<String, Object>> serviceData)
					//	consolidateMaps(serviceData,
					//	attribute.getKeys());
				mapToStore.put(GRAIN_WEIGHT, findWeight(serviceData));
				mapToStore.put(GRAIN_TYPE, grainType);
				if (mapToStore.containsKey("_id")) {
					mapToStore.remove("_id");
				}
				System.out.println("mapToStore: " + mapToStore);
				storageData.add(mapToStore);
			}
		}
		client.storeData(storageData);
	}

	protected abstract void consolidateMaps(Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, KeyDef keyDef, LinkedList<String> dataPath);
	
//TODO remake this function
	protected Map<String, Object> generateConsolidatedMap(Map<String, Object> templateData, Map<String,Object> consolidatedMap,
			LinkedList<String> path, List<KeyDef> statKeys,
			 Set<Map<String, Object>> serviceData) {
		
		for (String key : templateData.keySet()) {
			path.addLast(key);
			KeyDef matchingKeyDef = findMatchingPath(path, statKeys);
			if (matchingKeyDef != null) {
				consolidateMaps(consolidatedMap, serviceData, matchingKeyDef, path);
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
		int weight = 0;
		for (Map<String, Object> map : serviceData) {
			if (map.get(GRAIN_WEIGHT) == null) {
				weight++;
			} else {
				weight += (Integer) map.get(GRAIN_WEIGHT);
			}
		}
		return weight;
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
//								System.out.println("paths match: " + path
//										+ " vs " + keyString);
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
				/*
				 * TODO: regexpression code if
				 * (keyString.get(defIndex).equals("**") && defIndex <
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
					// TODO better exception saying we hit raw data
					// throw exception;
				}
			}
			index++;
		}
	}

	protected Set<Map<String, Object>> serviceFilter(String name) {
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterType);
		query.put(name, keyVals);
		// TODO FOR TESTING ONLY
		startDate = new Date(0L);
		endDate = new Date();
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
			LOGGER.warn("AbstractGrain.statToDouble(Object stat)-unexpected Object type");
		}
		return input;
	}
}
