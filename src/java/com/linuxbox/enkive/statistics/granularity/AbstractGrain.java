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
	
	private Object getStat(String statName, String method, Set<Map<String, Object>> data){
		DescriptiveStatistics statsMaker = new DescriptiveStatistics();
		int totalWeight = 0;
		Object temp = null;
		for(Map<String, Object> map: data){
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
			
			if(input >= 0){
				if(method.equals(GRAIN_AVG)){
					Integer tempWeight = (Integer)map.get(GRAIN_WEIGHT);
					if(tempWeight == null){
						tempWeight = 1;
					}
					
					totalWeight += tempWeight;
//					System.out.println("tempWeight: " + tempWeight);
//					System.out.println("totalWeight: " + totalWeight);
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
/*			System.out.println("statName: " + statName);
			System.out.println("totalWeightAVG: " + totalWeight);
			System.out.println("tempAVG: " + temp);
			System.out.println("tempClass: " + temp.getClass());
*/			return injectType(temp, statsMaker.getSum()/totalWeight);
		}
		if(method.equals(GRAIN_STD_DEV)){
			return injectType(temp, statsMaker.getStandardDeviation());
		}
		
//		System.out.println("getStat-null-vals: " + temp + " " + statName + " " + totalWeight + " " + method);
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
	
//TODO: slow
	private Map<String, Object> makeSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());
		for(String key:attribute.getKeys().keySet()){
			Set<String> call = attribute.getKeys().get(key);
			for(String method, call){
//				System.out.println("makeSnapshot values: " + key + " " + call + " " + serviceData);
				result.put(key, getStat(key, call, serviceData));
			}
			//TODO: make this method work for many stats
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
//		System.out.println("RUNNING consolidateData()");		
		//1. build set for each service (filter by service name)
		    Set<Map<String,Object>> storageData = new HashSet<Map<String,Object>>();
			for(GathererAttributes attribute: client.getAttributes()){
				String name = attribute.getName();
				//TODO: Figure out a way to not have the check for collectionstatsservice
				if(name == "CollectionStatsService"){
					continue;
				}
				Set<Map<String,Object>> serviceData = serviceFilter(name);
			//3. loop through that set to apply AVG, ADD, MAX, etc.
				if(!serviceData.isEmpty()){
					Map<String, Object> newGrain = makeSnapshot(attribute, serviceData);
					newGrain.put(GRAIN_TYPE, grainType);
					newGrain.put(GRAIN_WEIGHT, findWeight(serviceData));
					storageData.add(newGrain);
				}
//			System.out.println("consolidateData()-storagedata: " + storageData);
			//4. store the snapshot
			client.storeData(storageData);			
		}
	}
}
