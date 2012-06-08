package com.linuxbox.enkive.statistics.services;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class StatsGathererService extends AbstractService {
	SchedulerFactory sf;
	Scheduler sched;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	protected Map<String, GathererInterface> statsGatherers = null;

	public void setUp() throws SchedulerException, ParseException {
		sf = new StdSchedulerFactory();
		sched = sf.getScheduler();
		gatherStats(null);
		sched.start();
	}

	public StatsGathererService() throws SchedulerException, ParseException {
		setUp();
	}

	public StatsGathererService(Map<String, GathererInterface> statsGatherers)
			throws SchedulerException, ParseException {
		this.statsGatherers = statsGatherers;
		setUp();
	}

	public StatsGathererService(String serviceName, GathererInterface service)
			throws SchedulerException, ParseException {
		statsGatherers = new HashMap<String, GathererInterface>();
		statsGatherers.put(serviceName, service);
		setUp();
	}

	public Map<String, GathererInterface> getStatsGatherers(){
		return statsGatherers;
	}
	public void addGatherer(String name, GathererInterface gatherer) {
		statsGatherers.put(name, gatherer);
	}

	public void removeGatherer(String name) {
		statsGatherers.remove(name);
	}

	public Set<Map<String, Object>> gatherStats() throws ParseException,
			SchedulerException {
		return gatherStats(null);
	}

	public Set<Map<String, Object>> gatherStats(Map<String, String[]> map)
			throws ParseException, SchedulerException {
		// if no map given create one that is for all the known gatherers
		/*
		 * if (map == null) { map = new HashMap<String, String[]>(); for (String
		 * str : statsGatherers.keySet()) { map.put(str, null); } }
		 * 
		 * for(String name : map.keySet()){ JobDetail jd = new
		 * JobDetail(name+"Job", "jobs", statsGatherers.get(name).getClass());
		 * System.out.println(statsGatherers.get(name).getClass() + " " +
		 * statsGatherers.get(name).getSchedule()); CronTrigger ct = new
		 * CronTrigger(name+"Trigger", "triggers",
		 * statsGatherers.get(name).getSchedule()); sched.scheduleJob(jd,ct); }
		 */
		/*
		 * Set<Map<String, Object>> statsSet = createSet(); for (String name :
		 * map.keySet()) { // long attributeTime =
		 * statsGatherers.get(name).attributes // .getNextRunTime(); long
		 * currTime = System.currentTimeMillis(); // if not time skip that one
		 * // if (attributeTime > currTime) { // continue; // } Map<String,
		 * Object> temp = createMap(); if (map.get(name) != null) temp.put(name,
		 * statsGatherers.get(name).getStatistics(map.get(name))); else
		 * temp.put(name, statsGatherers.get(name).getStatistics());
		 * statsSet.add(temp); } return statsSet;
		 */return null;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsRuntimeGatherer runProps = new StatsRuntimeGatherer("SERVICENAME", "CRONEXPRESSION");
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs", "AttachName", "cronExpress");
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive", "attachName", "cronexpress");
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive", "collName", "cronExpression");

		Map<String, GathererInterface> gatherers = new HashMap<String, GathererInterface>();
		gatherers.put("runProps", runProps);
		gatherers.put("attachProps", attachProps);
		gatherers.put("dbProps", dbProps);
		gatherers.put("collProps", collProps);

		String[] keys = { STAT_TYPE, STAT_NAME, STAT_DATA_SIZE,
				STAT_AVG_ATTACH, STAT_FREE_MEMORY };

		Map<String, String[]> serviceKeys = new HashMap<String, String[]>();
		serviceKeys.put("runProps", keys);
		serviceKeys.put("attachProps", keys);
		serviceKeys.put("dbProps", keys);
		serviceKeys.put("collProps", keys);

		try {
			@SuppressWarnings("unused")
			StatsGathererService service = new StatsGathererService(gatherers);
		} catch (Exception e) {
			System.exit(0);
			// TODO: handle this
		}
	}
}
