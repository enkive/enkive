package com.linuxbox.util.dbinfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A version of DbInfo that maintains access to a set of other DbInfos.
 */
public class MultiDbInfo implements DbInfo {
	protected String serviceName;
	protected List<DbInfo> subinfos;
	protected Map<String,DbInfo> byNameMap;
	
	public MultiDbInfo(String serviceName, List<DbInfo> subinfos) {
		this.serviceName = serviceName;
		this.subinfos = subinfos;
		this.byNameMap = new HashMap<String,DbInfo>(subinfos.size());
		for (DbInfo info : subinfos) {
			this.byNameMap.put(info.getServiceName(), info);
		}
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}
	
	public DbInfo getByServiceName(String serviceName) {
		return byNameMap.get(serviceName);
	}
}
