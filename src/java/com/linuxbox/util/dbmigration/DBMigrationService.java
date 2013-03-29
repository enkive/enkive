package com.linuxbox.util.dbmigration;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DBMigrationService implements ApplicationContextAware{
	ApplicationContext applicationContext;
	protected List<DBMigrator> migrators;

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext = context;
	}
	
	@PostConstruct
	public void runMigrators(){
		for(DBMigrator migrator: migrators){
			//TODO run all the migrations
		}
	}
}
