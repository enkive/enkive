/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
		this.schedule = "0 0 * * * ?";
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

//		if (ConsolidationTimeDefs.DAY.isMatch()) {
			LOGGER.trace("ConsolidationRunner day() starting");
			dayConsolidator.setDates();
			dayConsolidator.storeConsolidatedData();
			LOGGER.trace("ConsolidationRunner day() finished");
//		}

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
		// don't run remover until consolidation is done
		remover.cleanAll();
	}
}
