package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class AbstractGrain implements Grain{
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Integer filterObj;
	protected int grainType;
	protected Date startDate;
	protected Date endDate;
	
	public AbstractGrain(StatsClient client){
		this.client = client;
		System.out.println("starting abstract client");
		setFilterString();
		setDates();
		consolidateData();
		System.out.println("finishing abstract client");
	}
	
	protected abstract void setFilterString();
	
	protected abstract void setDates();
	
	private double getValue(String key, Map<String, Object> map){
		double result = -1;
//TODO		System.out.println("map: " + map);
		if(map.get(key) instanceof Integer){
			result = (double)((Integer)map.get(key)).intValue();
		}
		else if(map.get(key) instanceof Long){
			result = (double)((Long)map.get(key)).longValue();
		}
		else if(map.get(key) instanceof Double){
			result = ((Double)map.get(key)).doubleValue();
		}
//TODO		System.out.println("result: " + result);
		return result;
	}
	
	private Object injectType(Object example, double value){
		Object result = null;
		if(example instanceof Integer){
			result = (int)value;
		}
		else if(example instanceof Long){
			result = (long)value;
		}
		else if(example instanceof Double){
			result = value;
		}
		
//TODO		System.out.println("injection:");
//		System.out.println("example: " + example);
//		System.out.println("RESULTTTT: " + result);
			
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Object getStat(String statName, String method, Set<Map<String, Object>> data){
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		int totalWeight = 0;
		Object temp = null;
		for(Map<String, Object> map: data){
			if(map.containsKey(statName))
				temp = map.get(statName);
			
			double input = -1;
			
			if(temp instanceof Integer){
				input = (double)((Integer)temp).intValue();
			}
			else if(temp instanceof Long){
				input = (double)((Long)temp).longValue();
			}
			else if(temp instanceof Double){
				input = ((Double)temp).doubleValue();
			}
			else if(temp instanceof Map){
				if(grainType == GRAIN_HOUR){ //using raw data
					input = getValue(statName, (Map<String,Object>)temp);
					temp = ((Map<String, Object>)temp).get(statName);
				}
				else{//using pre-granulated data
					input = getValue(method, (Map<String,Object>)temp);
					temp = ((Map<String, Object>)temp).get(method);
				}
			}
			
			if(input >= 0){
				if(method.equals(GRAIN_AVG)){
					Integer tempWeight = (Integer)map.get(GRAIN_WEIGHT);
					if(tempWeight == null){
						tempWeight = 1;
					}
					
					totalWeight += tempWeight;
					input = input * tempWeight;
				}
				statsMaker.addValue(input);
			}
		}
		
		if(method.equals(GRAIN_SUM)){
			return injectType(temp, statsMaker.getSum());
		}
		if(method.equals(GRAIN_MAX)){
			return injectType(temp, statsMaker.getMax());
		}
		if(method.equals(GRAIN_MIN)){
			return injectType(temp, statsMaker.getMin());
		}
		if(method.equals(GRAIN_AVG)){
			return injectType(temp, statsMaker.getSum()/totalWeight);
		}
		if(method.equals(GRAIN_STD_DEV)){
			return injectType(temp, statsMaker.getStandardDeviation());
		}
		System.out.println("returning null...");
		return null;
	}
	
	private Set<Map<String,Object>> serviceFilter(String name){
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterObj);
		query.put(name, keyVals);
		//TODO FOR TESTING ONLY
		startDate = new Date(0L);
		endDate = new Date();
		Set<Map<String,Object>> result = client.queryStatistics(query, startDate, endDate);
		return result;
	}
/*	
	private Map<String, Object> makeEmbeddedSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		for(String key: serviceData.iterator().next().keySet()){
			if(!(key.equals(STAT_TIME_STAMP) || key.equals(GRAIN_WEIGHT) || key.equals("_id") || key.equals(STAT_SERVICE_NAME) || serviceData == null || key.equals(GRAIN_TYPE))){
				Set<Map<String,Object>> collStats= new HashSet<Map<String,Object>>();
				for(Map<String, Object> data: serviceData){
					if(data.get(key) != null ){
//TODO						System.out.println("data: " + data);
//						System.out.println("key: " + key);
						Map<String, Object> statMap = (HashMap<String, Object>)data.get(key);
						collStats.add(statMap);
					}
				}
				result.put(key, makeSnapshot(attribute, collStats));
			}
		}
		result.put(STAT_SERVICE_NAME, attribute.getName());
		Set<String> kz = new HashSet<String>();
		kz.add(GRAIN_AVG);
		kz.add(GRAIN_MIN);
		kz.add(GRAIN_MAX);
		Map<String, Set<String>> timeKeys = new HashMap<String, Set<String>>();
		timeKeys.put(STAT_TIME_STAMP, kz);
		GathererAttributes a = new GathererAttributes(attribute.getName(), "", timeKeys);
		result.putAll(makeSnapshot(a, serviceData));
		return result;
	}
	
	private Map<String, Object> makeSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){		
		Map<String, Object> result = new HashMap<String, Object>();
		for(String key:attribute.getKeys().keySet()){
			Set<String> call = attribute.getKeys().get(key);
			if(call != null){
				Map<String, Object> map = new HashMap<String, Object>();	
				for(String method: call){
					if(getStat(key, method, serviceData) != null){
						map.put(method, getStat(key, method, serviceData));
						result.put(key,  map);
					}
					else{
						String vars = "sn:"+attribute.getName()+" key:"+key+" method:"+method;
						LOGGER.warn("getStat() returned null-vars: "+vars);
					}
				}
			}
			else{
				Object obj = serviceData.iterator().next().get(key);
				if(obj != null)
					result.put(key, obj);
			}
		}
		return result;
	}
*/	
	
	private String asteriskHandler(List<String> path, int index){
		try{
			while(path.get(index).equals("*")){
				index++;
			}
			return path.get(index + 1);
		} catch(IndexOutOfBoundsException e){
			return null;
		}
	}
	

	
	private boolean pathMatches(List<String> path, List<KeyDef> keys){
		for(KeyDef def: keys){//get one key definition
			boolean isMatch = true;
			int pathIndex = 0;
			int defIndex = 0;
			List<String> keyString = def.getKey();
			while(pathIndex < path.size()){//run through it to compare to path
				if(defIndex >= keyString.size()){
//					System.out.println("defIndex >= keys.size()");
//					System.out.println("paths don't match: " + path + " vs " + keyString);
					break;
				}
				String str = keyString.get(defIndex);
				if(str.equals("*")){
//					System.out.println("str*: " + str);
//					System.out.println("path*: " + path.get(pathIndex));
					defIndex++;
					str = keyString.get(defIndex);
				
					if(path.contains(str)){
						for(;pathIndex < path.size(); pathIndex++){//jumps to matching index
							if(path.get(pathIndex).equals(str)){
								break;
							}
						}
					}
					else{
//						System.out.println("does not contain");
//						System.out.println("paths don't match: " + path + " vs " + keyString);
						isMatch = false;
						break;
					}				
				}
//				System.out.println("str: " + str);
//				System.out.println("path: " + path.get(pathIndex));
				if(path.get(pathIndex).equals(str)){
//					System.out.println("if");
					pathIndex++;
					defIndex++;
				}
				else{
//					System.out.println("paths don't match: " + path + " vs " + keyString);
					isMatch = false;
					break;
				}
			}
			if(isMatch){
				System.out.println("paths match: " + path + " vs " + keyString);
				return true;
			}
		}
		return false;//no matches found
	}
	
	private Object getDataVal(Map<String, Object> dataMap, List<String> path){
		Object map = dataMap;
		System.out.println("getDV-path: " + path);
		for(String key: path){
			System.out.println("getDV-key: " + key);
			if(((Map<String, Object>)map).containsKey(key)){
				if(((Map<String, Object>)map).get(key) instanceof Map){
					System.out.println("Dataval is a map: " + map);
					map = ((Map<String, Object>)map).get(key);
				}
				else{
					System.out.println("returning dataVal: " + ((Map<String, Object>)map).get(key));
					return ((Map<String, Object>)map).get(key);
				}
			}
		}
		
		System.out.println("getDataVal returning null");
		return null;
	}
/*	
	//TODO: get the object & apply the methods as outlined by the List<String> methods
	@SuppressWarnings("unchecked")
	private Object getStater(List<String> path, List<String> methods, Set<Map<String, Object>> data){		
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		int totalWeight = 0;
		Object temp = null;
		//TODO: make this work
		Set<List<String>> dataPaths = findPaths(, new List<String>(), List<KeyDef> keys, Map<String, Object> result);
		//1. recurse to find set of paths
		//2. populate new map with paths that can't be consolidated
		//3. remove keys of path that no longer apply (ie. sn, ns, etc.)
		//4. loop over paths and add to statmaker
		//5. loop over methods to populate map with max, min, etc.
		//6. store in new map
		
		for(Map<String, Object> map: data){
			temp = getDataVal(map, path);
			
			double input = -1;
			
			if(temp instanceof Integer){
				input = (double)((Integer)temp).intValue();
			}
			else if(temp instanceof Long){
				input = (double)((Long)temp).longValue();
			}
			else if(temp instanceof Double){
				input = ((Double)temp).doubleValue();
			}
			else
				System.out.println("");
			}
			else if(temp instanceof Map){
				if(grainType == GRAIN_HOUR){ //using raw data
					input = getValue(statName, (Map<String,Object>)temp);
					temp = ((Map<String, Object>)temp).get(statName);
				}
				else{//using pre-granulated data
					input = getValue(method, (Map<String,Object>)temp);
					temp = ((Map<String, Object>)temp).get(method);
				}
			}
			
			if(input >= 0){
				if(method.equals(GRAIN_AVG)){
					Integer tempWeight = (Integer)map.get(GRAIN_WEIGHT);
					if(tempWeight == null){
						tempWeight = 1;
					}
					
					totalWeight += tempWeight;
					input = input * tempWeight;
				}
				statsMaker.addValue(input);
			}
		}
		
		if(method.equals(GRAIN_SUM)){
			return injectType(temp, statsMaker.getSum());
		}
		if(method.equals(GRAIN_MAX)){
			return injectType(temp, statsMaker.getMax());
		}
		if(method.equals(GRAIN_MIN)){
			return injectType(temp, statsMaker.getMin());
		}
		if(method.equals(GRAIN_AVG)){
			return injectType(temp, statsMaker.getSum()/totalWeight);
		}
		if(method.equals(GRAIN_STD_DEV)){
			return injectType(temp, statsMaker.getStandardDeviation());
		}
		System.out.println("returning null...");
		return null;

	} 
*/	
	private Set<List<String>> findPathSet(Map<String, Object> data, LinkedList<String> path, List<KeyDef> statKeys, Set<List<String>> result){
		for(String key: data.keySet()){
			path.addLast(key);
			if(pathMatches(path, statKeys)){
//	/			System.out.println("key: " + key);
//				System.out.println("findPath-adding path: " + path);
				result.add(new ArrayList<String>(path));
			}
			else{//recurse again
				if(data.get(key) instanceof Map){
					findPathSet((Map<String, Object>)data.get(key), path, statKeys, result);
				}
			}
			path.removeLast();
		}
//		System.out.println("findPath-result: " + result);
		return result;
	}
	
/*	
	private List<String> findPath(Map<String, Object> map, List<String> path, List<KeyDef> keys){
		for(String key: map.keySet()){
			path.add(key);
			if(pathMatches(path, keys)){
				System.out.println("findPath-adding path: " + path);
				result.add(path);
				path = new LinkedList<String>();
			}
			else{//recurse again
				if(map.get(key) instanceof Map){
					return findPath((Map<String, Object>)map.get(key), path, keys, result);
				}
			}
			path.remove(path.size()-1);
		}
		System.out.println("findPath-Return last path");
		return result;
	}
*/
	private Map<String, Object> consolidateMapHelper(Map<String, Object> map, List<String> path, List<KeyDef> keys, Map<String, Object> result){
		System.out.println("run...");
		for(String key: map.keySet()){
			path.add(key);
			if(pathMatches(path, keys)){
				result.put(key, map.get(key));
			}
			else{//recurse again
				if(map.get(key) instanceof Map){
					result.put(key, consolidateMapHelper((Map<String, Object>)map.get(key), path, keys, new HashMap<String, Object>()));
				}
			}
			path.remove(path.size()-1);
		}
		return result;
	}
	
	protected Map<String, Object> consolidateMap(Map<String, Object> map, List<KeyDef> keys){
		Set<List<String>> path = new HashSet<List<String>>();
//		Map<String, Object> result = consolidateMapHelper(map, path, keys, new HashMap<String, Object>());
//		System.out.println("consolidateMap()-result: " + result);
		path = findPathSet(map, new LinkedList<String>(), keys, new HashSet<List<String>>());
		System.out.println("findPath: " + path);
//		System.out.println("getDataVal(): " + getDataVal(map, path));
		return null;
	}
	
	protected void printObj(Map<String, Object> map){
		System.out.println("{");
		for(String key: map.keySet()){
			Object temp = map.get(key);
			if(temp instanceof Integer){
				System.out.println(key + ": " + temp);
			}
			else if(temp instanceof Long){
				System.out.println(key + ": " + temp);
			}
			else if(temp instanceof Double){
				System.out.println(key + ": " + temp);
			}
			else if(temp instanceof Map){
				System.out.println(key + "{ ");
				printObj((Map<String, Object>)temp);
				System.out.println("\n}");
			}
			else{
				System.out.println(key + ": " + temp);
			}
		}
		System.out.println("}");
	}
	
	protected int findWeight(Set<Map<String, Object>> serviceData){
		int weight = 0;
		for(Map<String, Object> map: serviceData){
			if(map.get(GRAIN_WEIGHT) == null)
				weight++;
			else{
				weight+= (Integer)map.get(GRAIN_WEIGHT);
			}
		}
		return weight;
	}
		
	public void consolidateData(){	
		System.out.println("consolidateData()");
		//build set for each service
		Set<Map<String,Object>> storageData = new HashSet<Map<String,Object>>();
		for(GathererAttributes attribute: client.getAttributes()){
			String name = attribute.getName();
			System.out.println("name: " + name);
			Set<Map<String,Object>> serviceData = serviceFilter(name);
			System.out.println("ServiceData: " + serviceData);
			if(!serviceData.isEmpty()){
				Object dataArray[] = serviceData.toArray();
				Map<String, Object> newGrain = (Map<String, Object>) dataArray[0];
				System.out.println("for ref (Map): " + newGrain); 
				consolidateMap(newGrain, attribute.getKeys());
				if(name.equals("CollectionStatsService")){
					System.out.println("exit");
					System.exit(0);
				}
/*					//TODO call a function where you will do recursive walk down

					if(name.equals("CollectionStatsService")){
						newGrain = makeEmbeddedSnapshot(attribute, serviceData);
					}
					else{
						newGrain = makeSnapshot(attribute, serviceData);
					}
					newGrain.put(STAT_SERVICE_NAME, name);
					newGrain.put(GRAIN_TYPE, grainType);
					newGrain.put(GRAIN_WEIGHT, findWeight(serviceData));
					storageData.add(newGrain);
					client.storeData(storageData);
*/
			}
		}
	}
}
