package com.linuxbox.enkive.statistics.granularity;


import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.removal.Remove;

public class GrainRunner {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.GrainRunner");
	StatsClient client;
	Scheduler scheduler;
	String schedule;
	Remove remover;
	
	public GrainRunner(StatsClient client, Scheduler scheduler, String schedule, Remove remover){
		this.client = client;
		this.scheduler = scheduler;
		this.schedule = schedule;
		this.remover = remover;
		LOGGER.info("GrainRunner Initialized");
	}
	
	@PostConstruct
	public void init() throws Exception{
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName("GrainRunnerJob");
		jobDetail.setTargetMethod("run");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName("GrainRunnerTrigger");
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(schedule);
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
	}
	
	public void run(){
		LOGGER.info("GrainRunner run() starting");
		if(Granularity.HOUR.isMatch()){
			LOGGER.trace("GrainRunner hour() starting");
			new HourGrain(client);
			LOGGER.trace("GrainRunner hour() finished");
		}
/*		
//		if(Granularity.DAY.isMatch()){
		if(true){
			LOGGER.trace("GrainRunner day() starting");
			new DayGrain(client);
			LOGGER.trace("GrainRunner day() finished");
		}
		
		if(Granularity.WEEK.isMatch()){
//		if(true){
			LOGGER.trace("GrainRunner week() starting");
			new WeekGrain(client);
			LOGGER.trace("GrainRunner week() finished");
		}
		
		if(Granularity.MONTH.isMatch()){
//		if(true){
			LOGGER.trace("GrainRunner month() starting");
			new MonthGrain(client);
			LOGGER.trace("GrainRunner month() finished");
		}
		LOGGER.info("GrainRunner run() finished");
		
		remover.cleanAll();
*/
	}
}
