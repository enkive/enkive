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
	
	//TODO null exception handling?
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
			else if(temp instanceof Map<?,?>){
				System.out.println("tempMAP: " + temp);
				input = getValue(statName, (Map<String,Object>)temp);
			}
			else{
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
		System.out.print(name + "serviceFilter: " + result + "\t\t");
		System.out.println("query " + query);
		return result;
	}
	
//TODO: very slow
	private Map<String, Object> makeSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());
		//TODO fix to avoid collection stats
		for(String key:attribute.getKeys().keySet()){
			Set<String> call = attribute.getKeys().get(key);
			if(call != null){
				for(String method: call){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(method, getStat(key, method, serviceData));
					result.put(key, method);
				}
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
			//TODO: Figure out a way to not have the check for collectionstatsservice
			Set<Map<String,Object>> serviceData = serviceFilter(name);
			if(name.equals("CollectionStatsService")){
				System.out.println("collectionstatsService-" + serviceData);
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
		}
	}
}
