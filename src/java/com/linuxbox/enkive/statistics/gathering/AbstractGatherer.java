package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.AbstractCreator;
import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;

public abstract class AbstractGatherer extends AbstractCreator implements
		GathererInterface {
	protected GathererAttributes attributes;
	protected Scheduler scheduler;
	protected StatsStorageService storageService;
	protected List<String> keys;
	private String serviceName;
	private String schedule;

	public AbstractGatherer(String serviceName, String schedule) {
		this.serviceName = serviceName;
		this.schedule = schedule;
	}

	@Override
	public GathererAttributes getAttributes() {
		return attributes;
	}

	@Override
	public abstract Map<String, Object> getStatistics();

	@Override
	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null) {
			return getStatistics();
		}
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String key : keys) {
			if (stats.get(key) != null) {
				selectedStats.put(key, stats.get(key));
			}
		}

		selectedStats.put(STAT_SERVICE_NAME, attributes.getName());

		if (selectedStats.get(STAT_TIME_STAMP) == null
				&& stats.get(STAT_TIME_STAMP) != null) {
			selectedStats.put(STAT_TIME_STAMP, stats.get(STAT_TIME_STAMP));
		} else {
			selectedStats.put(STAT_TIME_STAMP,
					new Date(System.currentTimeMillis()));
		}

		return selectedStats;
	}

	@PostConstruct
	protected void init() throws Exception {
		// create attributes
		attributes = new GathererAttributes(serviceName, schedule,
				keyBuilder(keys));
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

	protected List<KeyDef> keyBuilder(List<String> keyList) {
		List<KeyDef> keys = new LinkedList<KeyDef>();
		if (keyList != null) {
			for (String key : keyList) {
				keys.add(new KeyDef(key));
			}
		}
		return keys;
	}

	/*
	 * NOAH: what's a creator, how's it used? I tried to find a caller of this
	 * method but couldn't find any.
	 */
	protected Set<String> makeCreator(String... methodTypes) {
		Set<String> result = new HashSet<String>();
		for (String methodName : methodTypes) {
			result.add(methodName);
		}
		return result;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void setStorageService(StatsStorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void storeStats() throws StatsStorageException {
		if (getStatistics() != null) {
			storageService.storeStatistics(attributes.getName(),
					getStatistics());
		}
	}

	public void setKeys(List<String> keys) {
		this.keys = keys;
	}
}
