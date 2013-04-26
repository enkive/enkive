package com.linuxbox.util.dbinfo;


public abstract class AbstractDBInfo implements DBInfo {
	final protected String serviceName;

	public AbstractDBInfo(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}
}
