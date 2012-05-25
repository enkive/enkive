package com.linuxbox.enkive.statistics.services;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.linuxbox.enkive.archiver.schedule_detete_when_done.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererConfigBean;
import com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Configuration
public class StatsGathererService extends AbstractService {
	SchedulerFactory sf;
	Scheduler sched;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.mongodb");
	Map<String, AbstractGatherer> statsGatherers = null;

	public StatsGathererService(Map<String, AbstractGatherer> statsGatherers) {
		this.statsGatherers = statsGatherers;
		this.StatsGathererService();
	}

	public StatsGathererService(String serviceName, AbstractGatherer service) {
		statsGatherers = new HashMap<String, AbstractGatherer>();
		statsGatherers.put(serviceName, service);
		StatsGathererService();
	}

	// all stats
	public void addGatherer(String name, AbstractGatherer gatherer) {
		statsGatherers.put(name, gatherer);
	}

	public Set<Map<String, Object>> gatherStats() {
//		return gatherStats(null);
		return null;
	}
	
	public StatsGathererService() throws Exception {
		sf=new StdSchedulerFactory();
	  	sched=sf.getScheduler();
	  	CronTrigger ct=new CronTrigger("cronTrigger","group2","0/5 * * * * ?");
	  	gatherStats(null);
	  	sched.start();
	}
	  
	// designated services & keys
	public Set<Map<String, Object>> gatherStats(Map<String, String[]> map) throws ParseException, SchedulerException {
		
		if (map == null) {
			map = new HashMap<String, String[]>();
			for (String str : statsGatherers.keySet()) {
				map.put(str, null);
			}
		}
		for(String name : map.keySet()){
//			GathererConfigBean configBean = new GathererConfigBean();
//			configBean.createBean(name, statsGatherers.get(name), "storeStats");
			JobDetail jd = new JobDetail(name, "group1", AbstractGatherer.class);
			CronTrigger ct = new CronTrigger(name+"trig", "group2", "0/5 * * * * ?");
			sched.scheduleJob(jd,ct);
/*			JobDetail job = null;
			try {
				job = configBean.createJobDetail(name, statsGatherers.get(name), "storeStats");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(job == null){
				System.out.println("enkive caught fire");
				System.exit(0);
			}
			
			try {
				configBean.createTrigger(name, job, statsGatherers.get(name).getSchedule());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/		}

/*
		Set<Map<String, Object>> statsSet = createSet();
		for (String name : map.keySet()) {
//			long attributeTime = statsGatherers.get(name).attributes
//					.getNextRunTime();
			long currTime = System.currentTimeMillis();
			// if not time skip that one
//			if (attributeTime > currTime) {
//				continue;
//			}
			Map<String, Object> temp = createMap();
			if (map.get(name) != null)
				temp.put(name,
						statsGatherers.get(name).getStatistics(map.get(name)));
			else
				temp.put(name, statsGatherers.get(name).getStatistics());
			statsSet.add(temp);
		}
		return statsSet;
*/		return null;
	}

	public static void main(String args[]) throws UnknownHostException,
			MongoException {
		StatsRuntimeGatherer runProps = new StatsRuntimeGatherer();
		StatsMongoAttachmentsGatherer attachProps = new StatsMongoAttachmentsGatherer(
				new Mongo(), "enkive", "fs");
		StatsMongoDBGatherer dbProps = new StatsMongoDBGatherer(new Mongo(),
				"enkive");
		StatsMongoCollectionGatherer collProps = new StatsMongoCollectionGatherer(
				new Mongo(), "enkive");

		Map<String, AbstractGatherer> gatherers = new HashMap<String, AbstractGatherer>();
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
		
		try{ 
			StatsGathererService service = new StatsGathererService(gatherers);			  
		 }catch(Exception e){}
		
		//System.out.println(service.gatherStats(null));
	}
}
