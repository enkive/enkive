package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.text.ParseException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.AbstractService;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.storage.StatsStorageException;

public abstract class AbstractGatherer extends AbstractService implements
		GathererInterface {
	public GathererAttributes attributes;
	public String schedule;
	public StatsStorageService storageService;
	public CronTriggerBean cronTrigger;
	public MethodInvokingJobDetailFactoryBean jobDetailFactory;
	protected String serviceName;
	
	public void setStorageService(StatsStorageService storageService){
		this.storageService = storageService;
	}
	
	public void setSchedule(String schedule){
		this.schedule = schedule;
	}
	
	@PostConstruct
	public void init() throws ParseException{
		GathererConfigBean beanCreator = new GathererConfigBean();
		JobDetail job = beanCreator.createJobDetail(serviceName, this, "storeStats");
		beanCreator.createTrigger(serviceName, schedule, job);
	}
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
