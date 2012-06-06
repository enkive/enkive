package com.linuxbox.enkive.statistics.gathering;

import java.text.ParseException;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

//@Configuration  
public class GathererConfigBean {
 //   @Bean
    public MethodInvokingJobDetailFactoryBean createMethodInvokingJobDetail(String service, Object obj, String method) throws ClassNotFoundException, NoSuchMethodException{
        System.out.println(" bean being created");
    	MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setBeanName(service + "FactoryBean");
        jobDetailFactory.setTargetObject(obj);
        jobDetailFactory.setTargetMethod(method);
        jobDetailFactory.setConcurrent(false);
        jobDetailFactory.afterPropertiesSet();
        System.out.println("bean created: " + jobDetailFactory);
        return jobDetailFactory;
    }
/*   	
    @Bean
    public CronTriggerBean createTriggerBean(String service, JobDetail job, String schedule) throws ParseException{
        CronTriggerBean trigger = new CronTriggerBean();
        trigger.setBeanName(service + "TriggerBean");
        trigger.setJobDetail(job);
        trigger.setCronExpression(schedule);
        System.out.println("bean created: " + trigger);
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

/*
	String service;
	String schedule;
	String method;
	Object obj;
	
	public GathererConfigBean(){
		//needed for cglib
	}
	
	@Autowired
	@Qualifier("quartz.service.name")
	public void setService(String service){
		this.service = service;
	}
	
	@Autowired
	@Qualifier("schedule")
	public void setSchedule(String schedule){
		this.schedule = schedule;
	}
	
	@Autowired
	@Qualifier("method")
	public void setMethod(String method){
		this.method = method;
	}
	
	@Autowired
	@Qualifier("obj")
	public void setObject(AbstractGatherer obj){
		this.obj = obj;
	}
	
	@Bean(name = "FactoryBean")
    public MethodInvokingJobDetailFactoryBean createMethodInvokingJobDetail() throws ClassNotFoundException, NoSuchMethodException{
        MethodInvokingJobDetailFactoryBean jobDetailFactory = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactory.setBeanName(service + "FactoryBean");
        jobDetailFactory.setTargetObject(obj);
        jobDetailFactory.setTargetMethod(method);
        jobDetailFactory.setConcurrent(false);
        jobDetailFactory.afterPropertiesSet();
        return jobDetailFactory;
    }

    @Bean(name = "TriggerBean")
    public CronTriggerBean createTriggerBean() throws ParseException, ClassNotFoundException, NoSuchMethodException{
        CronTriggerBean trigger = new CronTriggerBean();
        trigger.setBeanName(service + "TriggerBean");
        trigger.setJobDetail(createJobDetail());
        trigger.setCronExpression(schedule);
        return trigger;
    }	

    public CronTrigger createTrigger() throws ParseException, ClassNotFoundException, NoSuchMethodException{
        return createTriggerBean();
    }

    public JobDetail createJobDetail() throws ClassNotFoundException, NoSuchMethodException{
        return (JobDetail) createMethodInvokingJobDetail().getObject();
    }
*/
