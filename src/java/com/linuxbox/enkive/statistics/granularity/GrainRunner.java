package com.linuxbox.enkive.statistics.granularity;


import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class GrainRunner {
	StatsClient client;
	Scheduler scheduler;
	String schedule;
	
	
	public GrainRunner(StatsClient client, Scheduler scheduler, String schedule){
		this.client = client;
		this.scheduler = scheduler;
		this.schedule = schedule;
	}
	
	@PostConstruct
	public void init() throws Exception{
		System.out.println("GRAIN RUNNER RUNNING");
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
		if(Granularity.HOUR.isMatch()){
			System.out.println("HOUR RETURNED TRUE");
			new HourGrain(client);
		}
		
		if(Granularity.DAY.isMatch()){
			System.out.println("DAY RETURNED TRUE");
			new DayGrain(client);
		}
		
		if(Granularity.MONTH.isMatch()){
			System.out.println("MONTH RETURNED TRUE");
			new MonthGrain(client);
		}	
	}
}
