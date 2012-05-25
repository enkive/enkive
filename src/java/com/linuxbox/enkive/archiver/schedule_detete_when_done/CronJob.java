package com.linuxbox.enkive.archiver.schedule_detete_when_done;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.util.*;
public class CronJob implements Job {
  public void execute(JobExecutionContext arg0) throws JobExecutionException {
  System.out.println("Testing: time is  :"+new Date());
  }
}