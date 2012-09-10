package com.linuxbox.enkive.statistics.gathering;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_POINT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_INTERVAL;
import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.RawStats;
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

	@SuppressWarnings("unchecked")
	@Override
	public RawStats getStatistics(String[] intervalKeys, String[] pointKeys) throws GathererException {
		if (intervalKeys == null && pointKeys == null) {
			return getStatistics();
		}
		RawStats rawStats = getStatistics();
		Map<String, Object> data = rawStats.toMap();
		
		Map<String, Object> intervalData   = null;
		Map<String, Object> intervalResult = null;
		
		if(data.containsKey(STAT_INTERVAL) && intervalKeys != null && intervalKeys.length != 0){
			intervalData = (Map<String,Object>)data.get(STAT_INTERVAL);
			intervalResult = new HashMap<String, Object>();
			for(String statName: intervalKeys){
				if(intervalData.containsKey(statName)){
					intervalResult.put(statName, intervalData.get(statName));
				}
			}
		}
		
		Map<String, Object> pointData   = null;
		Map<String, Object> pointResult = null;
		
		if(data.containsKey(STAT_POINT) && pointKeys != null && pointKeys.length != 0){
			pointData = (Map<String,Object>)data.get(STAT_POINT);
			pointResult = new HashMap<String, Object>();
			for(String statName: pointKeys){
				if(pointData.containsKey(statName)){
					pointResult.put(statName, pointData.get(statName));
				}
			}
		}
		
		Date start = rawStats.getStartDate();
		Date end   = rawStats.getEndDate();
		RawStats result = new RawStats(intervalResult, pointResult, start, end);
		
		return result;
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
