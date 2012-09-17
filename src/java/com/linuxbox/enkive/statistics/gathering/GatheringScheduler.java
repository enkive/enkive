package com.linuxbox.enkive.statistics.gathering;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

public class GatheringScheduler {
	protected String name = "";
	protected List<GathererInterface> gatherers = new LinkedList<GathererInterface>();
	protected CronExpression schedule;
	protected Scheduler scheduler;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.GatheringScheduler");
	
	public GatheringScheduler(String name, List<GathererInterface> gatherers, Scheduler scheduler, String schedule){
		this.name = name;
		this.gatherers = gatherers;
		this.scheduler = scheduler;
		try {
			this.schedule = new CronExpression(schedule);
			// create factory
			MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
			jobDetail.setTargetObject(this);
			jobDetail.setName(name+"job");
			jobDetail.setTargetMethod("gatherAndStoreStats");
			jobDetail.setConcurrent(false);
				jobDetail.afterPropertiesSet();
	
			// create trigger
			CronTriggerBean trigger = new CronTriggerBean();
			trigger.setBeanName(name+"bean");
			trigger.setJobDetail((JobDetail) jobDetail.getObject());
			trigger.setCronExpression(schedule);
			trigger.afterPropertiesSet();
			// add to schedule defined in spring xml
			scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
		} catch (SchedulerException e) {
			LOGGER.error("Schedule Exception in " + name, e);
		} catch (ClassNotFoundException e) {
			LOGGER.error("ClassNotFound Exception in " + name, e);
		} catch (NoSuchMethodException e) {
			LOGGER.error("NoSuchMethod Exception in " + name, e);
		} catch (ParseException e){
			LOGGER.error("Could not parse schedule in " + name + " (" + schedule.toString() + ")", e );
		} catch (Exception e) {
			LOGGER.error("Exception in " + name, e);
		}
	}
	
	public void gatherAndStoreStats(){
		for(GathererInterface gatherer: gatherers){
			try {
				gatherer.storeStats();
			} catch (GathererException e) {
				LOGGER.error("GathererException in " + name + " for " + gatherer.getAttributes().getName(), e);
			}
		}
	}
}
