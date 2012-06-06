package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.text.ParseException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;


public abstract class AbstractGatherer extends AbstractService implements
		GathererInterface{
	protected String schedule;
	protected String serviceName;
	protected Scheduler scheduler;
	protected StatsStorageService storageService;

	public void setServiceName(String serviceName){
		this.serviceName = serviceName;
	}
	
	public void setScheduler(Scheduler scheduler){
		this.scheduler = scheduler;
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

	@PostConstruct
	public void init() throws Exception{		
		//create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName(serviceName);
		jobDetail.setTargetMethod("storeStats");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		//create trigger
		CronTriggerBean trigger = new CronTriggerBean();
        trigger.setBeanName(serviceName);
        trigger.setJobDetail((JobDetail) jobDetail.getObject());
        trigger.setCronExpression(schedule);
        trigger.afterPropertiesSet();
        
		//add to schedule
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);

	}
	
	public void storeStats() throws StatsStorageException{
		System.out.println(serviceName + " was stored");
		storageService.storeStatistics(serviceName, getStatistics());
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

		return selectedStats;
	}
}
