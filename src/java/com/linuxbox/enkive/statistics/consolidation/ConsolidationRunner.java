package com.linuxbox.enkive.statistics.consolidation;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.removal.RemovalJob;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class ConsolidationRunner {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.ConsolidationRunner");
	StatsClient client;
	RemovalJob remover;
	String schedule;
	Scheduler scheduler;
	Consolidator hourConsolidator;
	Consolidator dayConsolidator;
	Consolidator weekConsolidator;
	Consolidator monthConsolidator;

	public ConsolidationRunner(StatsClient client, Scheduler scheduler,
			String schedule, RemovalJob remover) {
		this.client = client;
		this.scheduler = scheduler;
		this.schedule = schedule;
		this.remover = remover;
		hourConsolidator = new HourConsolidator(client);
		dayConsolidator = new DayConsolidator(client);
		weekConsolidator = new WeekConsolidator(client);
		monthConsolidator = new MonthConsolidator(client);
		LOGGER.info("ConsolidationRunner Initialized");
	}

	@PostConstruct
	public void init() throws Exception {
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName("ConsolidationRunnerJob");
		jobDetail.setTargetMethod("run");
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName("ConsolidationRunnerTrigger");
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(schedule);
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
	}

	public void run() {
		LOGGER.info("ConsolidationRunner run() starting");
		if (ConsolidationTimeDefs.HOUR.isMatch()) {
			LOGGER.trace("ConsolidationRunner hour() starting");
			hourConsolidator.setDates();
			hourConsolidator.storeConsolidatedData();
			LOGGER.trace("ConsolidationRunner hour() finished");
		}

		if (ConsolidationTimeDefs.DAY.isMatch()) {
			LOGGER.trace("ConsolidationRunner day() starting");
			dayConsolidator.setDates();
			dayConsolidator.storeConsolidatedData();
			LOGGER.trace("ConsolidationRunner day() finished");
		}
		
		if (ConsolidationTimeDefs.WEEK.isMatch()) {
			LOGGER.trace("ConsolidationRunner week() starting");
			weekConsolidator.setDates();
			weekConsolidator.storeConsolidatedData();
			LOGGER.trace("ConsolidationRunner week() finished");
		}

		if (ConsolidationTimeDefs.MONTH.isMatch()) {
			LOGGER.trace("ConsolidationRunner month() starting");
			monthConsolidator.setDates();
			monthConsolidator.storeConsolidatedData();
			LOGGER.trace("ConsolidationRunner month() finished");
		}
		LOGGER.info("ConsolidationRunner run() finished");
		//don't run remover until consolidation is done
		remover.cleanAll();
	}
}
