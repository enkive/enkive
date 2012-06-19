package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public abstract class AbstractGatherer extends AbstractService implements
		GathererInterface {
	protected GathererAttributes attributes;
	protected StatsStorageService storageService;
	protected Scheduler scheduler;
	
	public AbstractGatherer(String serviceName, String schedule){
		attributes = new GathererAttributes(serviceName, schedule, keyBuilder());
	}
	
	@PostConstruct
	protected void init() throws Exception {
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName(attributes.getName());
		jobDetail.setTargetMethod("storeStats");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName(attributes.getName());
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(attributes.getSchedule());
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
	}
	
	protected abstract Map<String, Set<String>> keyBuilder();
	
	public abstract Map<String, Object> getStatistics();
	
	public void setStorageService(StatsStorageService storageService) {
		this.storageService = storageService;
	}
	
	public void setScheduler(Scheduler scheduler){
		this.scheduler = scheduler;
	}
	
	protected Set<String> makeCreator(String ... methodTypes){
		Set<String> result = new HashSet<String>();
		for(String methodName: methodTypes){
			result.add(methodName);
		}
		return result;
	}

	public GathererAttributes getAttributes(){
		return attributes;
	}

	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null)
			return getStatistics();
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String key : keys) {
			if (stats.get(key) != null)
				selectedStats.put(key, stats.get(key));
		}
		
		selectedStats.put(STAT_SERVICE_NAME, attributes.getName());
		
		if (selectedStats.get(STAT_TIME_STAMP) == null && stats.get(STAT_TIME_STAMP) != null){
			selectedStats.put(STAT_TIME_STAMP, stats.get(STAT_TIME_STAMP));
		}
		else
			selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

		return selectedStats;
	}
	
	public void storeStats() throws StatsStorageException {
		if(getStatistics() != null){
			storageService.storeStatistics(attributes.getName(), getStatistics());
		}
	}
}
