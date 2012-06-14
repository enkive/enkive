package com.linuxbox.enkive.statistics.removal;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import com.linuxbox.enkive.statistics.services.StatsClient;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.*;
public class Remove {
	private final String method = "cleanAll";
	StatsClient client;
	Scheduler scheduler;
	String schedule;
	private int rawBuff   =  1;
	private int hrBuff    = 48;
	private int dayBuff   = 30;
	private int wkBuff    = 10;
	private int monthBuff = 24;
	private Date filter;
	
	public void setRawBuff(int rawBuff){
		this.rawBuff = rawBuff;
	}
	
	public void setHrBuff(int hrBuff){
		this.hrBuff = hrBuff;
	}
	
	public void setDayBuff(int dayBuff){
		this.dayBuff = dayBuff;
	}
	
	public void setWkBuff(int wkBuff){
		this.wkBuff = wkBuff;
	}
	
	public void setMonthBuff(int monthBuff){
		this.monthBuff = monthBuff;
	}
	
	public Remove(StatsClient client, Scheduler scheduler, String schedule){
		this.client = client;
		this.scheduler = scheduler;
		this.schedule = schedule;
	}
		
	private void setDate(char time){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		switch(time){
			case 'm'://month
				cal.add(Calendar.MONTH, -monthBuff);
				break;
			case 'w'://week
				cal.add(Calendar.WEEK_OF_YEAR, -wkBuff);
				break;
			case 'd'://day
				cal.add(Calendar.DATE, -dayBuff);
				break;
			case 'h'://hour
				cal.add(Calendar.HOUR, -hrBuff);
				break;
			case 'r'://raw
				cal.add(Calendar.HOUR, -rawBuff);
		}
		filter = cal.getTime();
	}
	
	private void cleaner(char interval, String type){
		setDate(interval);
		Set<Map<String,Object>> data = client.queryStatistics(null, new Date(0L), filter);
		Set<Object> deletionSet = new HashSet<Object>();
		for(Map<String, Object> map: data){
			String gType = (String)map.get(GRAIN_TYPE);
			if(gType != null){
				if(gType.equals(type)){
					deletionSet.add(map.get("_id"));
				}		
			}
			else if(type == null){
				deletionSet.add(map.get("_id"));
			}
		}
		client.remove(deletionSet);
	}
	
	private void cleanRaw(){
		if(rawBuff != -1)
			cleaner('r', null);
	}
	
	private void cleanHour(){
		if(hrBuff != -1)
			cleaner('h', GRAIN_HOUR);
	}
	
	private void cleanDay(){
		if(dayBuff != -1)
			cleaner('d', GRAIN_DAY);
	}

	private void cleanWeek(){
		if(wkBuff != -1)
			cleaner('w', GRAIN_WEEK);
	}

	private void cleanMonth(){
		if(monthBuff != -1)
			cleaner('m', GRAIN_MONTH);
	}
	
	public void cleanAll(){
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
		
		
	}
	
	@PostConstruct
	public void init() throws Exception{
		// create factory
		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
		jobDetail.setTargetObject(this);
		jobDetail.setName("RemoverJob");
		jobDetail.setTargetMethod(method);
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
