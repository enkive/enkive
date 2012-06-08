package com.linuxbox.enkive.statistics.granularity;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class AbstractGrain {
	protected StatsClient client;
	protected String time;
	protected String schedule;
	protected Scheduler scheduler;
	
	@PostConstruct
	public void init() throws Exception {
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName(time + "jobBean");
		jobDetail.setTargetMethod("consolidateData");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName(time + "triggerBean");
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(schedule);
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);

	}
	
	private Object add(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		for(Map<String, Object> map: data){
			if((Double)map.get(statName) < 0){
				continue;
			}
			
			if(first){
				result = map.get(statName);
				first = false;
			}
			else{
				if(result instanceof Integer)
					result = (Integer)result + (Integer)map.get(statName);
				else if(result instanceof Long)
					result = (Long)result + (Long)map.get(statName);
				else if(result instanceof Double)
					result = (Double)result + (Double)map.get(statName);
			}	
		}
		return result;
	}
	
	private Object max(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		for(Map<String, Object> map: data){
			if((Double)map.get(statName) < 0){
				continue;
			}
			
			if(first){
				result = map.get(statName);
				first = false;
			}
			else{
				if(result instanceof Integer){
					if((Integer)result < (Integer)map.get(statName));
						result = map.get(statName);
				}
				else if(result instanceof Long){
					if((Long)result < (Long)map.get(statName));
						result = map.get(statName);
				}
				else if(result instanceof Double){
					if((Double)result < (Double)map.get(statName));
						result = map.get(statName);
				}
			}	
		}	
		return result;
	}
	
	private Object avg(String statName, Set<Map<String,Object>> data){
		Object result = null;
		boolean first = true;
		int counter = 0;
		for(Map<String, Object> map: data){
			if((Double)map.get(statName) < 0){
				continue;
			}
			
			if(first){
				result = map.get(statName);
				first = false;
			}
			else{
				if(result instanceof Integer){
					result = (Integer)result + (Integer)map.get(statName);
					counter++;
				}
				else if(result instanceof Long){
					result = (Long)result + (Long)map.get(statName);
					counter++;
				}
				else if(result instanceof Double){
					result = (Double)result + (Double)map.get(statName);
					counter++;
				}
			}	
		}
		if(result instanceof Integer)
			return (Integer)result/counter;
		if(result instanceof Long)
			return (Long)result/counter;
		if(result instanceof Double)
			return (Double)result/counter;
		
		return new Integer(-1); 
	}
	
	private Set<Map<String,Object>> serviceFilter(String name, Set<Map<String, Object>> data){
		Set<Map<String,Object>> result = new HashSet<Map<String, Object>>();
		for(Map<String, Object> map: data){
			if(map.get(STAT_NAME).equals(name)){
				result.add(map);
			}
		}
		return result;
	}
	
	private Map<String, Object> consolidateData(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		for(String key:attribute.getKeys().keySet()){
			String call = attribute.getKeys().get(key);
			if(call.equals("ADD")){
				result.put(key, add(key, serviceData));
			}
			else if(call.equals("AVG")){
				result.put(key, avg(key, serviceData));
			}
			else if(call.equals("MAX")){
				result.put(key, max(key, serviceData));
			}
			//TODO: make this work
			/*String[] calls = attribute.getKeys().get(key);
			for(String call: calls){
				if(call.equals("ADD")){
					result.put(key, add(key, serviceData));
				}
				else if(call.equals("AVG")){
					result.put(key, avg(key, serviceData));
				}
				else if(call.equals("MAX")){
					result.put(key, max(key, serviceData));
				}
			}
			*/
		}
		//3.5 TODO add new pairs (granulairity, weight, etc)
		
		return result;
	}
	
	public void consolidateData(){
		long time = System.currentTimeMillis();
		time = time - time%3600000;//TODO: Make non-hour specific
		if(Granularity.HOURLY.isMatch(new Date(time))){
			Date endDate = new Date(time);  
			Calendar cal = Calendar.getInstance();
		    cal.setTime(endDate);
		    cal.add(Calendar.DAY_OF_MONTH, -1);
		    Date startDate = cal.getTime();

			//1. query the database for all stats between dates
			Set<Map<String, Object>> data = client.queryStatistics(null, startDate, endDate);
			//2. build set for each service
			for(GathererAttributes attribute: client.getAttributes()){
				String name = attribute.getName();
				Set<Map<String,Object>> serviceData = serviceFilter(name, data);
			//3. loop through that set to apply AVG, ADD, MAX, etc.
				Map<String, Object> newGrain = consolidateData(attribute, serviceData);
			//4. remove all found objects from DB
				client.remove(data);
			//5. store the new consolidated grain
				data = new HashSet<Map<String,Object>>();//recycle variable
				data.add(newGrain);
				System.out.println("HOURLY TEST: " + data);
				client.storeData(data);
			}
		}
	}
}
