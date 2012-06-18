package com.linuxbox.enkive.statistics.granularity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;

public abstract class AbstractGrain implements Grain{
	protected StatsClient client;
	protected String filterString;
	protected String grainType;
	protected Date startDate;
	protected Date endDate;
	
	public void start(){
		setFilterString();
		setDates();
//		System.out.println("ActualDate: " + new Date(System.currentTimeMillis()));
//	    System.out.println("StartDate: " + startDate);
//	    System.out.println("EndDate: " + endDate);
		consolidateData();
	}
	
	protected abstract void setFilterString();
	
	protected abstract void setDates();
	
	private double getValue(String key, Map<String, Object> map){
		double result = -1;
		if(map.get(key) instanceof Integer){
			result = (double)((Integer)map.get(key)).intValue();
		}
		else if(map.get(key) instanceof Long){
			result = (double)((Long)map.get(key)).longValue();
		}
		else if(map.get(key) instanceof Double){
			result = ((Double)map.get(key)).doubleValue();
		}
		
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
				System.out.println("tempMAP: " + temp);
				if(map.get(GRAIN_TYPE).equals(GRAIN_HOUR)){ //using raw data
					input = getValue(statName, (Map<String,Object>)temp);
				}
				else{//using pre-granulated data
					input = getValue(method, (Map<String,Object>)temp);
				}
			}
			else{//error
				System.out.println("ELSE-" + statName + " temp: " + temp);
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
		
		return null;
	}
	
	private Set<Map<String,Object>> serviceFilter(String name){
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterString);
		query.put(name, keyVals);
		Set<Map<String,Object>> result = client.queryStatistics(query, startDate, endDate);
		return result;
	}
	
//TODO:
	private Map<String, Object> makeEmbeddedSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());		
		
		for(String key: serviceData.iterator().next().keySet()){
			if(!(key.equals(STAT_TIME_STAMP) || key.equals("_id") || key.equals(STAT_SERVICE_NAME) || serviceData == null)){
				Set<Map<String,Object>> collStats= new HashSet<Map<String,Object>>();
				for(Map<String, Object> data: serviceData){
					System.out.println("key: " + key);
					System.out.println("data: " + data);
					@SuppressWarnings("unchecked")//we know how it should be stored
					Map<String, Object> statMap = (HashMap<String, Object>)data.get(key);
					System.out.println("StatMap: " + statMap);
					Object obj = data.get(STAT_TIME_STAMP);
					if(obj != null)
						statMap.put(STAT_TIME_STAMP, obj);
					collStats.add(statMap);
//					System.out.println("collStats" + collStats);
				}
				result.put(key, makeSnapshot(attribute, collStats));
			}
		}
		System.out.println("makeEmbSnap-result:" + result);
		return result;
	}
	
	private Map<String, Object> makeSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){		
		Map<String, Object> result = new HashMap<String, Object>();
		if(!attribute.getName().equals("CollectionStatsService")){
				result.put(STAT_SERVICE_NAME, attribute.getName());
		}
		for(String key:attribute.getKeys().keySet()){
			Set<String> call = attribute.getKeys().get(key);
			if(call != null){
				Map<String, Object> map = new HashMap<String, Object>();
				for(String method: call){
					map.put(method, getStat(key, method, serviceData));
				}
				result.put(key, map);
			}
		}
		return result;
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
		//build set for each service
		Set<Map<String,Object>> storageData = new HashSet<Map<String,Object>>();
		for(GathererAttributes attribute: client.getAttributes()){
			String name = attribute.getName();
			Set<Map<String,Object>> serviceData = serviceFilter(name);
			
			if(!serviceData.isEmpty()){
				if(name.equals("CollectionStatsService")){
					Map<String, Object> newGrain;
					if(name.equals("CollectionStatsService")){//TODO: need a 'isEmbedded' key
						newGrain = makeEmbeddedSnapshot(attribute, serviceData);
						System.out.println("collectionStatsService-newGrain: " + newGrain);
					}
					else{
						newGrain = makeSnapshot(attribute, serviceData);
					}
					newGrain.put(GRAIN_TYPE, grainType);
					newGrain.put(GRAIN_WEIGHT, findWeight(serviceData));
					storageData.add(newGrain);
					client.storeData(storageData);
				}
			}
			else{
				System.out.println("filter empty: " + name);
			}
/*					
			if(name.equals("CollectionStatsService")){
				System.out.println("collectionstatsService-" + serviceData);
				makeEmbeddedSnapshot(attribute, serviceData);
				continue;
			}
			else if(!serviceData.isEmpty()){
				//generate a snapshot by applying AVG, ADD, MAX, etc.
				Map<String, Object> newGrain = makeSnapshot(attribute, serviceData);
				newGrain.put(GRAIN_TYPE, grainType);
				newGrain.put(GRAIN_WEIGHT, findWeight(serviceData));
				storageData.add(newGrain);
		//store the snapshot
				client.storeData(storageData);
			}
*/
		}
	}
}
