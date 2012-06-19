package com.linuxbox.enkive.statistics.removal;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_DAY;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_HOUR;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MONTH;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_WEEK;

import static com.linuxbox.enkive.statistics.removal.RemovalConstants.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.StatsClient;
public class Remove {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.removal");
	StatsClient client;
	Scheduler scheduler;
	String schedule;
	private int rawBuff   = REMOVAL_RAW_ID;
	private int hrBuff    = REMOVAL_HOUR_ID;
	private int dayBuff   = REMOVAL_DAY_ID;
	private int wkBuff    = REMOVAL_WEEK_ID;
	private int monthBuff = REMOVAL_MONTH_ID;
	private Date dateFilter;

	public void setRawBuff(int rawBuff){
		if(rawBuff >= REMOVAL_RAW_ID || rawBuff == -1){
			this.rawBuff = rawBuff;
		}
	}
	
	public void setHrBuff(int hrBuff){
		if(hrBuff >= REMOVAL_HOUR_ID || hrBuff == -1){
			this.hrBuff = hrBuff;
		}
	}
	
	public void setDayBuff(int dayBuff){
		if(dayBuff >= REMOVAL_DAY_ID || dayBuff == -1){
			this.dayBuff = dayBuff;
		}
	}
	
	public void setWkBuff(int wkBuff){
		if(wkBuff >= REMOVAL_WEEK_ID || wkBuff == -1){
			this.wkBuff = wkBuff;
		}
	}
	
	public void setMonthBuff(int monthBuff){
		if(monthBuff >= REMOVAL_MONTH_ID || monthBuff == -1){
			this.monthBuff = monthBuff;
		}
	}
	
	public Remove(StatsClient client, Scheduler scheduler, String schedule){
		this.client = client;
		this.scheduler = scheduler;
		this.schedule = schedule;
	}
		
	private void setDate(int time){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		switch(time){
			case REMOVAL_MONTH_ID://month
				cal.add(Calendar.MONTH, -monthBuff);
				break;
			case REMOVAL_WEEK_ID://week
				cal.add(Calendar.WEEK_OF_YEAR, -wkBuff);
				break;
			case REMOVAL_DAY_ID://day
				cal.add(Calendar.DATE, -dayBuff);
				break;
			case REMOVAL_HOUR_ID://hour
				cal.add(Calendar.HOUR, -hrBuff);
				break;
			case REMOVAL_RAW_ID://raw
				cal.add(Calendar.HOUR, -rawBuff);
		}
		dateFilter = cal.getTime();
	}
	//TODO: test	
	private void cleaner(int interval, int type){
		setDate(interval);
		Set<Map<String,Object>> data = client.queryStatistics(null, new Date(0L), dateFilter);
		Set<Object> deletionSet = new HashSet<Object>();
		for(Map<String, Object> map: data){
			Integer gType = (Integer)map.get(GRAIN_TYPE);
			if(gType != null){
				if(gType.equals(type)){
					deletionSet.add(map.get("_id"));
				}		
			}
			else if(type == 0){
				deletionSet.add(map.get("_id"));
			}
		}
		client.remove(deletionSet);
	}
	
	private void cleanRaw(){
		if(rawBuff != -1)
			cleaner(REMOVAL_RAW_ID, 0);
	}
	
	private void cleanHour(){
		if(hrBuff != -1)
			cleaner(REMOVAL_HOUR_ID, GRAIN_HOUR);
	}
	
	private void cleanDay(){
		if(dayBuff != -1)
			cleaner(REMOVAL_DAY_ID, GRAIN_DAY);
	}

	private void cleanWeek(){
		if(wkBuff != -1)
			cleaner(REMOVAL_WEEK_ID, GRAIN_WEEK);
	}

	private void cleanMonth(){
		if(monthBuff != -1)
			cleaner(REMOVAL_MONTH_ID, GRAIN_MONTH);
	}
	
	public void cleanAll(){
		LOGGER.info("Starting removal");
		Calendar c = Calendar.getInstance();
		cleanRaw();
		cleanHour();
		
		if(c.get(Calendar.HOUR) == 0){
			cleanDay();
			
			if(c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				cleanWeek();
			}
			
			if(c.get(Calendar.DAY_OF_MONTH) == 1){
				cleanMonth();
			}
		}
		
		LOGGER.info("Finished Removal");	
	}
	
	@PostConstruct
	public void init() throws Exception{
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName("RemoverJob");
		jobDetail.setTargetMethod(METHOD);
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();

		// create trigger
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName("RemoverTrigger");
		trigger.setJobDetail((JobDetail) jobDetail.getObject());
		trigger.setCronExpression(schedule);
		trigger.afterPropertiesSet();

		// add to schedule defined in spring xml
		scheduler.scheduleJob((JobDetail) jobDetail.getObject(), trigger);
	}
}
