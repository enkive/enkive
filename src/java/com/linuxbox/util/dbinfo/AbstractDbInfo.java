package com.linuxbox.util.dbinfo;


public abstract class AbstractDbInfo implements DbInfo {
	final protected String serviceName;

	public AbstractDbInfo(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}
}
