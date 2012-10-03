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
			.getLog("com.linuxbox.enkive.statistics.granularity.GrainRunner");
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
		LOGGER.info("GrainRunner Initialized");
	}

	@PostConstruct
	public void init() throws Exception {
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

	public void run() {
		LOGGER.info("GrainRunner run() starting");
		if (ConsolidationTimeDefs.HOUR.isMatch()) {
			LOGGER.trace("GrainRunner hour() starting");
			hourConsolidator.setDates();
			hourConsolidator.storeConsolidatedData();
			LOGGER.trace("GrainRunner hour() finished");
		}

		if (ConsolidationTimeDefs.DAY.isMatch()) {
			LOGGER.trace("GrainRunner day() starting");
			dayConsolidator.setDates();
			dayConsolidator.storeConsolidatedData();
			LOGGER.trace("GrainRunner day() finished");
		}

		if (ConsolidationTimeDefs.WEEK.isMatch()) {
			LOGGER.trace("GrainRunner week() starting");
			weekConsolidator.setDates();
			weekConsolidator.storeConsolidatedData();
			LOGGER.trace("GrainRunner week() finished");
		}

		if (ConsolidationTimeDefs.MONTH.isMatch()) {
			LOGGER.trace("GrainRunner month() starting");
			monthConsolidator.setDates();
			monthConsolidator.storeConsolidatedData();
			LOGGER.trace("GrainRunner month() finished");
		}
		LOGGER.info("GrainRunner run() finished");
		// don't run remover until consolidation is done
		remover.cleanAll();
	}
}
