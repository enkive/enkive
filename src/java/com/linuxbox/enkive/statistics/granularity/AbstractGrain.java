package com.linuxbox.enkive.statistics.granularity;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;
import static com.linuxbox.enkive.statistics.granularity.Grain_Constants.*;

public abstract class AbstractGrain implements Grain{
	protected StatsClient client;
	protected String filterString;
	protected String grainType;
	protected Date startDate;
	protected Date endDate;
	
	public void start(){
		setFilterString();
		setDates();
		System.out.println("ActualDate: " + new Date(System.currentTimeMillis()));
	    System.out.println("StartDate: " + startDate);
	    System.out.println("EndDate: " + endDate);
		consolidateData();
	}
	
	protected abstract void setFilterString();
	
	protected abstract void setDates();
	
	private boolean hasError(Object map.get(statName)){
		if(map.get(statName) instanceof Integer){
			if((Integer)map.get(statName) < 0)
				return true;
		}
		else if(map.get(statName) instanceof Long){
			if((Long)map.get(statName) < 0)
				return true;
		}
		else if(map.get(statName) instanceof Double){
			if((Double)map.get(statName) < 0)
				return true;
		}
		return false;
	}
	
	private Object getResult(Object result, Map<String, Object> map, String statName, String method){
		Object obj = map.get(statName);
		
		if(method == null){
			return result;
		}
		
		if(result == null){
			if(map.get(statName) instanceof Integer){
				result = (Integer)map.get(statName);
			}
			else if(map.get(statName) instanceof Long){
				result = (Long)map.get(statName);
			}
			else if(map.get(statName) instanceof Double){
				result = (Double)map.get(statName);
			}
			return result;
		}
		
		if(map.get(statName) instanceof Integer){
			if(method.equals(GRAIN_ADD)){
				result = (Integer)result + (Integer)map.get(statName);
			}
			if(method.equals(GRAIN_MAX)){
				if((Integer)result < (Integer)map.get(statName))
					result = map.get(statName);
			}
			if(method.equals(GRAIN_MIN)){
				if((Integer)result > (Integer)map.get(statName))
					result = map.get(statName);
			}
			if(method.equals(GRAIN_AVG)){
				result = (Integer)result + (Integer)map.get(statName);
			}
		}
		else if(map.get(statName) instanceof Long){
			if(method.equals("ADD") || method.equals("AVG")){
				result = (Long)result + (Long)map.get(statName);
			}
			if(method.equals("MAX")){
				if((Long)result < (Long)map.get(statName))
					result = map.get(statName);
			}
			if(method.equals("MIN")){
				if((Long)result > (Long)map.get(statName))
					result = map.get(statName);
			}
		}
		else if(map.get(statName) instanceof Double){
			if(method.equals("ADD") || method.equals("AVG")){
				result = (Double)result + (Double)map.get(statName);
			}
			if(method.equals("MAX")){
				if((Double)result < (Double)map.get(statName))
					result = map.get(statName);
			}
			if(method.equals("MIN")){
				if((Double)result > (Double)map.get(statName))
					result = map.get(statName);
			}
		}
		
		return result;
	}
	
	private Object add(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		for(Map<String, Object> map: data){
			if(hasError(map.get(statName))){
				continue;
			}
			
			if(first){
				result = map.get(statName);
				first = false;
			}
			else{
				result = getResult(result, map.get(statName), GRAIN_ADD);
			}	
		}
		return result;
	}
	
	private Object max(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		for(Map<String, Object> map: data){
			if(hasError(map.get(statName))){
				continue;
			}
			
			if(first){
				result = map.get(statName);
				first = false;
			}
			else{
				result = getResult(result, map.get(statName), GRAIN_MAX);
			}	
		}	
		return result;
	}
	
	private Object avg(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		int counter = 0;
		for(Map<String, Object> map: data){
			int weight = 0;
			if(hasError(map.get(statName))){			
				continue;
			}
			
			if(map.get(GRAIN_WEIGHT) == null)
				weight = 1;
			else
				weight = (Integer)map.get(GRAIN_WEIGHT);
			
			if(first){
				//TODO abstract this somehow? just pass in result & map and add/multi/etc.
				if(map.get(statName) instanceof Integer){
					result = (Integer)map.get(statName)*weight;
				}
				else if(map.get(statName) instanceof Long){
					result = (Long)map.get(statName)*weight;
				}
				else if(map.get(statName) instanceof Double){
					result = (Double)map.get(statName)*weight;
				}
				first = false;
				counter+=weight;
			}
			else{
				result = getResult(result, map.get(statName), GRAIN_AVG);
				counter+=weight;
			}	
		}
		
		if(result instanceof Integer){
			return (Integer)result/counter;
		}
		if(result instanceof Long){
			return (Long)result/counter;
		}
		if(result instanceof Double){
			return (Double)result/counter;
		}
		
		return new Integer(-1); 
	}
	
	private Set<Map<String,Object>> serviceFilter(String name, Set<Map<String, Object>> data){
		Set<Map<String,Object>> result = new HashSet<Map<String, Object>>();
		for(Map<String, Object> map: data){
			String statName = (String)map.get(STAT_SERVICE_NAME);
			//TODO: Figure out a way to not have the check for collectionstatsservice
			if(statName.equals(name) && !statName.equals("CollectionStatsService")){
				if((String)map.get(GRAIN_TYPE) == filterString)
					result.add(map);
			}
		}
		return result;
	}
	
	private Map<String, Object> makeSnapshot(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());
		for(String key:attribute.getKeys().keySet()){
			String call = attribute.getKeys().get(key);
			if(call == null){
				//do nothing
			}
			else if(call.equals("ADD")){
				result.put(key, add(key, serviceData));
			}
			else if(call.equals("AVG")){
				result.put(key, avg(key, serviceData));
			}
			else if(call.equals("MAX")){
				result.put(key, max(key, serviceData));
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
		System.out.println("RUNNING consolidateData()");		
	    
	    //1. query the database for all stats between dates
		Set<Map<String, Object>> data = client.queryStatistics(null, startDate, endDate);
		if(!data.isEmpty()){
		//2. build set for each service (filter by service name)
		    Set<Map<String,Object>> storageData = new HashSet<Map<String,Object>>();
			for(GathererAttributes attribute: client.getAttributes()){
				String name = attribute.getName();
				Set<Map<String,Object>> serviceData = serviceFilter(name, data);
			//3. loop through that set to apply AVG, ADD, MAX, etc.
				if(!serviceData.isEmpty()){
					Map<String, Object> newGrain = makeSnapshot(attribute, serviceData);
					newGrain.put(GRAIN_TYPE, grainType);
					newGrain.put(GRAIN_WEIGHT, findWeight(serviceData));
					storageData.add(newGrain);
				}
			}
			System.out.println("consolidateData()-storagedata: " + storageData);
			//4. store the snapshot
			client.storeData(storageData);			
		}
	}
}
