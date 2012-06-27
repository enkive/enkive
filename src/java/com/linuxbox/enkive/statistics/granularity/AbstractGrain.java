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
	protected boolean isEmbedded;
	protected Integer filterType;
	protected int grainType;
	protected Date startDate;
	protected Date endDate;

	public AbstractGrain(StatsClient client) {
		this.client = client;
		LOGGER.info("starting abstract client");
		setDates();
		setTypes();
		consolidateData();
		LOGGER.info("finishing abstract client");
	}

	protected abstract void setTypes();
	
	protected abstract void setDates();

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

	protected Map<String, Object> putOnPath(List<String> path,
			Map<String, Object> statsData, Map<String, Object> statToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, statToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				}
				else{
					//TODO better exception saying we hit raw data
//					throw exception;
				}
			}
			index++;
		}

		return statsData;
	}

	protected Object getDataVal(Map<String, Object> dataMap, List<String> path) {
		Object map = dataMap;
//		System.out.println("path: " + path);
//		System.out.println("dataMap: " + dataMap);		
		for (String key : path) {
			if (((Map<String, Object>) map).containsKey(key)) {
				if (((Map<String, Object>) map).get(key) instanceof Map) {
					map = ((Map<String, Object>) map).get(key);
				} else {
//					System.out.println("getDataVal() returning: " + ((Map<String, Object>) map).get(key));
					return ((Map<String, Object>) map).get(key);
				}
			}
		}
		return null;
	}

	private boolean pathMatches(List<String> path, List<KeyDef> keys) {
		
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
//			System.out.println(path + " vs " + keyString);
			if(keyString.size() > path.size()){
	//			System.out.println("keyString.size() > path.size()" + path + " vs " + keyString);
				isMatch = false;
				continue;
			}
			
			while (pathIndex < path.size()) {// run through it to compare to path
				if(defIndex >= keyString.size()){
	//				System.out.println("defIndex >= keyString.size() " + path + " vs " + keyString);
					isMatch = false;
					break;
				}
				
				while(keyString.get(defIndex).equals("*") && defIndex < keyString.size()){
					if(defIndex == keyString.size()-1){
						if(keyString.get(defIndex).equals("*")){
							if(defIndex == path.size()-1){
								System.out.println("paths match: " + path + " vs " + keyString);
								return true;
							}
							else{
//								System.out.println("paths don't match " + path + " vs " + keyString);
								isMatch = false;
								break;
							}
						}
					}
					else{
						defIndex++;
						pathIndex++;
						keyStr = keyString.get(defIndex);
						pathStr = path.get(pathIndex);
					}
				}
/*TODO: regexpression code		
				if (keyString.get(defIndex).equals("**") && defIndex < keyString.size()) {
					if(defIndex == keyString.size()-1){
//						System.out.println("3");
						break;
					}
					defIndex++;

					if (path.contains(keyString.get(defIndex))) {
						//jump to matching index
						for (; pathIndex < path.size(); pathIndex++) {
							if (path.get(pathIndex).equals(keyString.get(defIndex))) {
								break;
							}
						}
					} else {
//						return false;
//						System.out.println("5"+keyString.get(defIndex));
						isMatch = false;
						break;
					}
				}
*/
				if(pathIndex >= path.size()){
//					System.out.println("pathIndex >= path.size()" + path + " vs " + keyString);
					isMatch = false;
					break;
//					System.out.println("pathIndex >= path.size()...returning false");
				}
				
				keyStr = keyString.get(defIndex);
				pathStr = path.get(pathIndex);
				
				if(keyStr.equals(pathStr)){
					pathIndex++;
					defIndex++;
				} else{
//					System.out.println("paths do not match" + path + " vs " + keyString);
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				System.out.println("paths match: " + path + " vs " + keyString);
				return true;
			}// no matches found
		}
		return false;
	}

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

	protected Set<List<String>> findPathSet(Map<String, Object> data,
			LinkedList<String> path, List<KeyDef> statKeys,
			Set<List<String>> result) {
		for (String key : data.keySet()) {
			path.addLast(key);
//			System.out.println("findPathSet-path: " + path);
			if (pathMatches(path, statKeys)) {
				result.add(new ArrayList<String>(path));
			} else {
				if (data.get(key) instanceof Map) {
					findPathSet((Map<String, Object>) data.get(key), path,
							statKeys, result);
				}
			}
			path.removeLast();
		}
		return result;
	}

	protected abstract Map<String, Object> consolidateMaps(Set<Map<String, Object>> serviceData, List<KeyDef> keys);
	
	protected int findWeight(Set<Map<String, Object>> serviceData) {
		int weight = 0;
		for (Map<String, Object> map : serviceData) {
			if (map.get(GRAIN_WEIGHT) == null)
				weight++;
			else {
				weight += (Integer) map.get(GRAIN_WEIGHT);
			}
		}
		return weight;
	}

	public void consolidateData() {
		// build set for each service
		Set<Map<String,Object>> storageData = new
		HashSet<Map<String,Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			if(name.equals("CollectionStatsService")){
			Set<Map<String, Object>> serviceData = serviceFilter(name);
//			System.out.println("ServiceData: " + serviceData);
			if (!serviceData.isEmpty()) {
				consolidateMaps(serviceData, attribute.getKeys());
				storageData = new HashSet<Map<String, Object>>();
				Map<String, Object> mapToStore = consolidateMaps(serviceData, attribute.getKeys());
				mapToStore.put(GRAIN_WEIGHT, findWeight(serviceData));
				mapToStore.put(GRAIN_TYPE, grainType);
				if(mapToStore.containsKey("_id")){
					mapToStore.remove("_id");
				}
				System.out.println("mapToStore: " + mapToStore);
				storageData.add(mapToStore);
			}
			}
		}
		client.storeData(storageData);
	}
	
/*
	//TODO: stat.stat.* working
	public static void main(String args[]){
		KeyDef def = new KeyDef("*.hi");
		KeyDef thing = new KeyDef("hi");
		List<String> path = thing.getKey();
		
		boolean isMatch = true;
		int pathIndex = 0;
		int defIndex = 0;
		String keyStr;
		String pathStr;
		List<String> keyString = def.getKey();
		boolean deletMe = true;
		if(keyString.size() > path.size()){
			//return false;
			System.out.println("key too big");
			isMatch = false;
			 deletMe = false;
		}
		
		while (pathIndex < path.size() && deletMe) {// run through it to compare to path
			if(defIndex >= keyString.size()){
				//return false;
				System.out.println("defIndex >= keyString.size()...returning false");
				isMatch = false;
				break;
			}
			
			boolean deleteMe = false;
			
			while(keyString.get(defIndex).equals("*") && defIndex < keyString.size()){
				
				System.out.println(defIndex + "vs" + (keyString.size()-1));
				if(defIndex == keyString.size()-1){
					System.out.println("1");
					if(keyString.get(defIndex).equals("*")){
						System.out.println("2");
						if(defIndex == path.size()-1){
						//	return true;
							deleteMe = true;
							break;
						}
						else{
						//  return false;
							isMatch = false;
							deleteMe = true;
							break;
						}
					}
				}
				else if(!deleteMe){
					defIndex++;
					pathIndex++;
					System.out.println("3a");
					keyStr = keyString.get(defIndex);
					pathStr = path.get(pathIndex);
				}
			}
			
			if(deleteMe){//delete
				System.out.println("deleteMe!");
				break;
			}
			
			if (keyString.get(defIndex).equals("**") && defIndex < keyString.size()) {
				if(defIndex == keyString.size()-1){
					System.out.println("3");
					break;
				}
				defIndex++;

				if (path.contains(keyString.get(defIndex))) {
					System.out.println("4");
					//jump to matching index
					for (; pathIndex < path.size(); pathIndex++) {
						if (path.get(pathIndex).equals(keyString.get(defIndex))) {
							break;
						}
					}
				} else {
					System.out.println("5"+keyString.get(defIndex));
					isMatch = false;
					break;
				}
			}

			if(pathIndex >= path.size()){
				//return false;
				System.out.println("pathIndex >= path.size()...returning false");
				isMatch = false;
				break;
			}
			
			keyStr = keyString.get(defIndex);
			pathStr = path.get(pathIndex);
			
			System.out.println(keyStr + " vs. " + pathStr);
			if(keyStr.equals(pathStr)){
				pathIndex++;
				defIndex++;
			} else{
				//return false;
				System.out.println("!keyStr.equals(pathStr)...returning false");
				isMatch = false;
				break;
			}
		}
		if (isMatch) {
			System.out.println("paths match: " + path + " vs " + keyString);
		}
	}
*/
}
