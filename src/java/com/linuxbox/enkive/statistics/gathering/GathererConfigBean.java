package com.linuxbox.enkive.statistics.gathering;

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;


@Configuration	
public class GathererConfigBean {
	@Bean
	public JobDetailBean createBean(String serviceName, Object service, String method ){
		System.out.println(serviceName + " " + service + " " + method);
		return new JobDetailBean();
	}
	/*private String service;
	private String method;
	private String schedule;
	private Object obj;
	private JobDetail job;
	
	public void setService(String service){
		this.service = service;
	}
	public void setMethod(String method){
		this.method = method;
	}
	public void setSchedule(String schedule){
		this.schedule = schedule;
	}
	public void setObject(Object obj){
		System.out.println("obj:____________ " + obj);
		this.obj = obj;
	}
	public void setJobDetail(JobDetail job){
		this.job = job;
	}

	@Bean
	public MethodInvokingJobDetailFactoryBean createMethodInvokingJobDetail() throws ClassNotFoundException, NoSuchMethodException{
		MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
		jobDetailFactory.setBeanName(service + "Bean");
		jobDetailFactory.setName(service + "Job");
		jobDetailFactory.setGroup(service + "Group");
		jobDetailFactory.setConcurrent(true);
		System.out.println("obj" + obj);
		jobDetailFactory.setTargetObject(obj);
		if(obj == null)
			System.out.println("OBJ is NULL");
		//System.out.println(obj.getClass());
		//jobDetailFactory.setTargetClass(obj.getClass());
		jobDetailFactory.setTargetMethod(method);
		jobDetailFactory.afterPropertiesSet();
		System.out.println("IsSingleton: " + jobDetailFactory.isSingleton());
		return jobDetailFactory;
	}
	
	public JobDetail createJobDetail() throws ClassNotFoundException, NoSuchMethodException{
		return (JobDetail) createMethodInvokingJobDetail().getObject();
	}
	
	@Bean
	public CronTriggerBean createTriggerBean() throws ParseException{
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName(service + "Bean");
		trigger.setJobDetail(job);
		trigger.setCronExpression(schedule);
		return trigger;
	}
	
	public CronTrigger createTrigger() throws ParseException{
		return createTriggerBean();
	}
	*/
/*	@Bean
	public MethodInvokingJobDetailFactoryBean createMethodInvokingJobDetail(String service, Object obj, String method) throws ClassNotFoundException, NoSuchMethodException{
		MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
		jobDetailFactory.setBeanName(service + "Bean");
		jobDetailFactory.setName(service + "Job");
		jobDetailFactory.setGroup("group1");
		jobDetailFactory.setConcurrent(true);
		jobDetailFactory.setTargetObject(obj);
		jobDetailFactory.setTargetMethod(method);
		jobDetailFactory.afterPropertiesSet();
		return jobDetailFactory;
	}
	
	@Bean
	public CronTriggerBean createTriggerBean(String service, JobDetail job, String schedule) throws ParseException{
		CronTriggerBean trigger = new CronTriggerBean();
		trigger.setBeanName(service + "Bean");
		trigger.setJobDetail(job);
		trigger.setCronExpression(schedule);
		return trigger;
	}
	
	public CronTrigger createTrigger(String service, JobDetail job, String schedule) throws ParseException{
		return createTriggerBean(service, job, schedule);
	}
	
	public JobDetail createJobDetail(String service, AbstractGatherer obj, String method) throws ClassNotFoundException, NoSuchMethodException{
		return (JobDetail) createMethodInvokingJobDetail(service, obj, method).getObject();
	}
*/
}
