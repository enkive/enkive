package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.services.StatsStorageService;
import com.linuxbox.enkive.statistics.services.storage.StatsStorageException;
import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;

public abstract class AbstractGatherer implements GathererInterface {
	protected GathererAttributes attributes;
	protected Scheduler scheduler;
	protected StatsStorageService storageService;
	protected List<String> keys;
	private String serviceName;
	private String humanName;
	private String schedule;
	
	public AbstractGatherer(String serviceName, String humanName,
			String schedule, List<String> keys) throws GathererException {
		this.serviceName = serviceName;
		this.humanName = humanName;
		this.schedule = schedule;
		setKeys(keys);
	}

	@Override
	public GathererAttributes getAttributes() {
		return attributes;
	}

	@Override
	public abstract RawStats getStatistics() throws GathererException;

	@Override
	//TODO this needs work on repairing it via if only some are selected then you must
	//determine if they are ranged or not
	public RawStats getStatistics(String[] keys) throws GathererException {
/*		if (keys == null) {
			return getStatistics();
		}
		RawStats result = getStatistics();
		Map<String, Object> stats = result.getStatsMap();
		Map<String, Object> selectedStats = VarsMaker.createMap();
		for (String key : keys) {
			if (stats.get(key) != null) {
				selectedStats.put(key, stats.get(key));
			}
		}
		selectedStats.put(STAT_GATHERER_NAME, attributes.getName());
		result.setStatsMap(selectedStats);
		return result;
*/
		return null;
	}

	/**
	 * initialization method called to give quartz this gatherer
	 * 
	 * @throws Exception
	 */
	@PostConstruct
	protected void init() throws Exception {
		// create attributes
		attributes = new GathererAttributes(serviceName, humanName, schedule,
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

	/**
	 * builds the list of keyConsolidationHandlers in order to allow the raw
	 * data created by this gatherer to be consolidated
	 * 
	 * @param keyList
	 *            - a list of dot-notation formatted strings:
	 *            "coll.date:max,min,avg" the key's levels are specified by
	 *            periods and the key is separated from the methods by a colon.
	 *            An asterisk may be used as an 'any' to go down a level in a
	 *            map, such as: "*.date:max,min,avg"
	 * @return returns the instantiated consolidation list
	 * @throws GathererException
	 */
	protected List<ConsolidationKeyHandler> keyBuilder(List<String> keyList)
			throws GathererException {
		if (keyList == null) {
			throw new GathererException("keys were not set for " + serviceName);
		}

		List<ConsolidationKeyHandler> keys = new LinkedList<ConsolidationKeyHandler>();
		if (keyList != null) {
			for (String key : keyList) {
				keys.add(new ConsolidationKeyHandler(key));
			}
		}
		return keys;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void setStorageService(StatsStorageService storageService) {
		this.storageService = storageService;
	}

	@Override
	public void storeStats() throws GathererException {
		RawStats stats = getStatistics();
		if (stats != null) {
			try {
				storageService.storeStatistics(attributes.getName(), stats);
			} catch (StatsStorageException e) {
				throw new GathererException(e);
			}
		}
	}

	public void setKeys(List<String> keys) throws GathererException {
		this.keys = keys;
		// create attributes
		try {
			attributes = new GathererAttributes(serviceName, humanName,
					schedule, keyBuilder(keys));
		} catch (ParseException e) {
			throw new GathererException(
					"parseException in attributes constructor", e);
		}
	}
}
