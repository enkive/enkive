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
	private String grainType = "HOUR";//default to hour
	private Granularity grain;
	protected String schedule;
	protected Scheduler scheduler;
	//TODO Setters for these
	protected int hourBuff  = 0;
	protected int dayBuff   = 0;//default is kept 3 days
	protected int weekBuff  = 0;//only use if using "weeks"
	protected int monthBuff = 0;
	
	public void setClient(StatsClient client){
		this.client = client;
	}
	
	public void setScheduler(Scheduler scheduler){
		this.scheduler = scheduler;
	}
	
	public void setGrainType(String grainType){
		this.grainType = grainType;
	}
 	
	public void setSchedule(String schedule){
		this.schedule = schedule;
	}
	
	@PostConstruct
	public void init() throws Exception {		
		if(grainType.equals("MIN")){
			grain = Granularity.MIN;
			schedule = "0 * * * * ?"; //1st second any minute
		}
		else if(grainType.equals("HOUR")){
			grain = Granularity.HOUR;
			schedule = "* 0 * * * ?"; //1st minute any hour
		}
		else if(grainType.equals("DAY")){
			grain = Granularity.DAY;
			schedule = "* * 0 * * ?";//1st hour any day
		}
		else if(grainType.equals("WEEK")){
			grain = Granularity.WEEK;
			schedule = "* * * ? * SUN";
		}
		else if(grainType.equals("MONTH")){
			grain = Granularity.MONTH;
			schedule = "* * * 1 * ?"; //1st of any month
		}
		else if(grainType.equals("YEAR")){
			schedule = "* * * 1 1 ?"; //Jan 1st any year
			grain = Granularity.YEAR;
		}
		//TODO jibberish handling
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName(grainType + "jobBean");
		jobDetail.setTargetMethod("consolidateData");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName(grainType + "triggerBean");
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(schedule);
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
	}
	
	private boolean hasError(Object obj){
		if(obj instanceof Integer){
			if((Integer)obj < 0)
				return true;
		}
		else if(obj instanceof Long){
			if((Long)obj < 0)
				return true;
		}
		else if(obj instanceof Double){
			if((Double)obj < 0)
				return true;
		}
		return false;
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
			if(hasError(map.get(statName))){
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
//		System.out.println("avg-Data: " + data);
//		System.out.println("statName: " + statName);
		for(Map<String, Object> map: data){
			int weight = 0;
			if(hasError(map.get(statName))){
/*				System.out.println("Has error!!!");
				System.out.println("statName: "  + statName);
				System.out.println("Map: " + map);
*/				
				continue;
			}
			
			//TODO: fully implement weight (don't forget to reset it!)
			if(map.get("weight") == null)
				weight = 1;
			else
				weight = (Integer)map.get("weight");
			
			if(first){
				result = map.get(statName);
				first = false;
				counter+=weight;
			}
			else{
				if(result instanceof Integer){
					result = (Integer)result + (Integer)map.get(statName);
				}
				else if(result instanceof Long){
					result = (Long)result + (Long)map.get(statName);
				}
				else if(result instanceof Double){
					result = (Double)result + (Double)map.get(statName);
				}
				counter++;
			}	
		}
		
		if(result instanceof Integer){
//			System.out.println("Int: " + ((Integer)result).intValue());
			return (Integer)result/counter;
		}
		if(result instanceof Long){
//			System.out.println("Long: " + ((Long)result).longValue());
			return (Long)result/counter;
		}
		if(result instanceof Double){
//			System.out.println("Double: " + ((Double)result).doubleValue());
			return (Double)result/counter;
		}
		
		return new Integer(-1); 
	}
	
	private Set<Map<String,Object>> serviceFilter(String name, Set<Map<String, Object>> data){
		Set<Map<String,Object>> result = new HashSet<Map<String, Object>>();
//		System.out.println("serviceFilter()-data: " + data);
		for(Map<String, Object> map: data){
			String statName = (String)map.get(STAT_SERVICE_NAME);
			Granularity tempGrain = (Granularity)map.get("grain");
//			System.out.println(statName + " vs " + name);
//			if(statName == null){
//				System.out.println("mapisnull: " + map);
//			}
			if(statName.equals("CollectionStatsService")){
				continue;
			}
			if(statName.equals(name)){
//				System.out.println("Added map!");
				if(this.grain.isValidGrain(tempGrain))
					result.add(map);
			}
		}
		
//		System.out.println("serviceFilter(" + name + ")-result: " + result);
		return result;
	}
	
	private Map<String, Object> consolidator(GathererAttributes attribute, Set<Map<String,Object>> serviceData){
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(STAT_SERVICE_NAME, attribute.getName());
//		System.out.println("consolidateData(GA, Set)-serviceData: " + serviceData);
		for(String key:attribute.getKeys().keySet()){
			String call = attribute.getKeys().get(key);
//			System.out.println("consolidator-call: " + call);
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
//		System.out.println("consolidator-result: "+result);
		return result;
	}
	
	private Calendar applyBuffer(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.HOUR, -hourBuff);
		cal.add(Calendar.DATE, -dayBuff);
		cal.add(Calendar.MONTH, -monthBuff);
		return cal;
	}
	
	public void consolidateData(){
		System.out.println("RUNNING consolidateData()");		
		Calendar cal = applyBuffer();
		Date endDate = cal.getTime();
		//TODO: locking system
		switch(grain){
			case MIN: //for testing only
				cal.add(Calendar.MINUTE, -1);
				break;
			case HOUR://for consolidating all stats within an hour
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR, -1);
				break;
			case DAY:
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.DATE, -1);
				break;
			case WEEK:
				//TODO 
				break;
			case MONTH:
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.DATE, 1);
				cal.add(Calendar.MONTH, -1);
				break;
			case YEAR:
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.DATE, 1);
				cal.set(Calendar.MONTH, 0);
				cal.add(Calendar.YEAR, -1);
				break;
		}
		Date startDate = cal.getTime();
	    System.out.println("StartDate: " + startDate);
	    System.out.println("EndDate: " + endDate);
	    
	    
	    //1. query the database for all stats between dates
		Set<Map<String, Object>> data = client.queryStatistics(null, startDate, endDate);
		if(!data.isEmpty()){
		//2. build set for each service
		    Set<Map<String,Object>> storageData = new HashSet<Map<String,Object>>();
			for(GathererAttributes attribute: client.getAttributes()){
				String name = attribute.getName();
				Set<Map<String,Object>> serviceData = serviceFilter(name, data);
			//3. loop through that set to apply AVG, ADD, MAX, etc.
				if(!serviceData.isEmpty()){
					Map<String, Object> newGrain = consolidator(attribute, serviceData);
					newGrain.put("grain", grain);
					newGrain.put("weight", serviceData.size());
				//4. remove all found objects from DB
					client.remove(data);
					if(!client.queryStatistics(null, startDate, endDate).isEmpty()){
						System.out.println("error: this should be empty");
						System.out.println("data: " + data);
						System.out.println("querystats: " + client.queryStatistics(null, startDate, endDate));
						client.remove(client.queryStatistics(null, startDate, endDate));
						System.out.println("querystats: " + client.queryStatistics(null, startDate, endDate));
					}
				//5. store the new consolidated grain
					//TODO: non min specific
					storageData.add(newGrain);
				}
			}
			System.out.println("consolidateData()-storagedata: " + storageData);
//TODO:			client.storeData(data);			
		}
	}
}
