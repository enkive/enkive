/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.statistics.gathering;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
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
	protected List<Gatherer> gatherers = new LinkedList<Gatherer>();
	protected CronExpression schedule;
	protected int interval;
	protected Scheduler scheduler;
	protected MethodInvokingJobDetailFactoryBean jobDetail;
	protected CronTriggerBean trigger;
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.GatheringScheduler");

	protected Date lastFireTime = null;

	private void setCronExpression() throws ParseException {
		if (interval < 60) {
			this.schedule = new CronExpression("0 0/" + interval + " * * * ?");
		} else if (interval == 60) {
			this.schedule = new CronExpression("0 0 * * * ?");
		} else {
			interval = 15;
			LOGGER.warn("Interval exceeds 60--reverting to 15min default interval");
			this.schedule = new CronExpression("0 0/15 * * * ?");
		}
	}

	public GatheringScheduler(String name, List<Gatherer> gatherers,
			Scheduler scheduler, int interval) {
		this.interval = interval;
		this.gatherers = gatherers;
		this.scheduler = scheduler;
		try {
			setCronExpression();

			// define factory
			jobDetail = new MethodInvokingJobDetailFactoryBean();
			jobDetail.setTargetObject(this);
			jobDetail.setName(name + "job");
			jobDetail.setTargetMethod("gatherAndStoreStats");
			jobDetail.setConcurrent(false);
			jobDetail.afterPropertiesSet();

			// define trigger
			this.trigger = new CronTriggerBean();
			trigger.setBeanName(name + "TrigBean");
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
		} catch (ParseException e) {
			LOGGER.error("Could not parse schedule in " + name + " ("
					+ schedule + ")", e);
		} catch (Exception e) {
			LOGGER.error("Exception in " + name, e);
		}
	}

	private Date getEndTime() {
		Date endTime = new Date();
		long intervalMS = interval * 60 * 1000;
		if (lastFireTime != null) {
			endTime.setTime(lastFireTime.getTime() + intervalMS);
		} else {
			long r = endTime.getTime() % intervalMS;
			endTime.setTime(endTime.getTime() - r);
		}
		return endTime;
	}

	private Date getStartTime(Date endDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(endDate);
		c.add(Calendar.MINUTE, -interval);
		return c.getTime();
	}

	public void gatherAndStoreStats() {
		Date endDate = getEndTime();
		Date startDate = getStartTime(endDate);
		lastFireTime = endDate;

		for (Gatherer gatherer : gatherers) {
			try {
				gatherer.storeStats(gatherer.getStatistics(startDate, endDate));
			} catch (GathererException e) {
				LOGGER.error("GathererException in GatheringScheduler for "
						+ gatherer.getAttributes().getName(), e);
			}
		}
	}
}
