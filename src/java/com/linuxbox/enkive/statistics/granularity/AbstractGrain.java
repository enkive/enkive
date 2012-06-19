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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public abstract class AbstractGrain implements Grain{
	protected StatsClient client;
	protected Integer filterObj;
	protected int grainType;
	protected Date startDate;
	protected Date endDate;
	
	public AbstractGrain(StatsClient client){
		this.client = client;
		setFilterString();
		setDates();
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
//				System.out.println("tempMAP: " + temp);
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
		keyVals.put(GRAIN_TYPE, filterObj);
		query.put(name, keyVals);
		Set<Map<String,Object>> result = client.queryStatistics(query, startDate, endDate);
		System.out.println("filterObj: " + filterObj);
		System.out.println("result: " + result);
		return result;
	}
	
	private Map<String, Object> makeEmbeddedSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());		
		
		for(String key: serviceData.iterator().next().keySet()){
			if(!(key.equals(STAT_TIME_STAMP) || key.equals("_id") || key.equals(STAT_SERVICE_NAME) || serviceData == null)){
				Set<Map<String,Object>> collStats= new HashSet<Map<String,Object>>();
				for(Map<String, Object> data: serviceData){
					if(data.get(key) != null){
						@SuppressWarnings("unchecked")//we know how it should be stored
						Map<String, Object> statMap = (HashMap<String, Object>)data.get(key);
						statMap.put(STAT_TIME_STAMP, data.get(STAT_TIME_STAMP));
						collStats.add(statMap);
					}
				}
				result.put(key, makeSnapshot(attribute, collStats));
			}
		}
//		System.out.println("makeEmbSnap-result:" + result);
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
					if(name.equals("CollectionStatsService")){
						newGrain = makeEmbeddedSnapshot(attribute, serviceData);
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
		}
	}
}
