package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;


public abstract class AbstractGatherer extends AbstractService implements
		GathererInterface, Job {
//	public GathererAttributes attributes;
	public String schedule = "0/5 * * * * ?"; //default every 5 seconds
	public StatsStorageService storageService;
//	public CronTriggerBean cronTrigger;
//	public MethodInvokingJobDetailFactoryBean jobDetailFactory;
	protected String serviceName;
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException{
		System.out.println(serviceName + " " + getStatistics());
		try {
			storeStats();
		} catch (StatsStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}
	
	public void setStorageService(StatsStorageService storageService){
		this.storageService = storageService;
	}
	
	public void setSchedule(String schedule){
		this.schedule = schedule;
	}
	
	public String getSchedule(){
		return schedule;
	}
/*
	@PostConstruct
	public void init() throws ParseException, ClassNotFoundException, NoSuchMethodException{

		System.out.println("\nCreating bean for schedule in " + serviceName);
		GathererConfigBean beanCreator = new GathererConfigBean();
		beanCreator.setService(serviceName);
		System.out.println(serviceName);
		beanCreator.setMethod("storeStats");
		System.out.println("storeStats");
		beanCreator.setObject(this);
		if(this != null)
			System.out.println("!this is " + this);
		else
			System.out.println("!this is null");
		
		beanCreator.setSchedule(schedule);
		System.out.println(schedule);
		JobDetail job = beanCreator.createJobDetail();
		System.out.println(job);
		beanCreator.setJobDetail(job);
		beanCreator.createTrigger();
		System.out.println("...finished");

		System.out.println("\nCreating bean for schedule in " + serviceName);
		GathererConfigBean beanCreator = new GathererConfigBean();
		JobDetail job = beanCreator.createJobDetail(serviceName, this, "storeStats");
		beanCreator.createTrigger(serviceName, schedule, job);
		System.out.println("f");


	}
*/
	
/*	
	protected void setAttributes() {
		Map<String, Object> defaultMap = null;// getStatistics();
		long start = System.currentTimeMillis();// initialized at
		long interval = 3600000;// hour
	//	attributes = new GathererAttributes();//interval, start, defaultMap);
	}
*/	
	public void storeStats() throws StatsStorageException{
		storageService.storeStatistics(this.serviceName, getStatistics());
	}
	public abstract Map<String, Object> getStatistics();

	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null)
			return getStatistics();
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String key : keys) {
			if (stats.get(key) != null)
				selectedStats.put(key, stats.get(key));
		}
		if (selectedStats.get(STAT_TIME_STAMP) != null)
			selectedStats.put(STAT_TIME_STAMP,
					selectedStats.get(STAT_TIME_STAMP));
		else
			selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

//		attributes.incrementTime();
		return selectedStats;
	}
}
