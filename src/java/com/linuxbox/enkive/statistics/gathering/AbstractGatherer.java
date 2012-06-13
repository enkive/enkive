package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;

public abstract class AbstractGatherer extends AbstractService implements
		GathererInterface {
	protected GathererAttributes attributes;
	protected StatsStorageService storageService;
	protected Scheduler scheduler;
	
	public void setStorageService(StatsStorageService storageService) {
		this.storageService = storageService;
	}
	
	public void setScheduler(Scheduler scheduler){
		this.scheduler = scheduler;
	}

	@PostConstruct
	public void init() throws Exception {
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

	public GathererAttributes getAttributes(){
		return attributes;
	}
	
	public void storeStats() throws StatsStorageException {
		System.out.println(attributes.getName() + " was stored");
		storageService.storeStatistics(attributes.getName(), getStatistics());
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
