package com.linuxbox.util.dbinfo;

/**
 * Encapsulates the access to a database. We need to separate this from the
 * services themselves because the migrators also need them. And rather than
 * doing it twice it'd be better to do it once in a Spring configuration XML
 * file that gets loaded by the services and by the migrators.
 */
public interface DBInfo {
	public String getServiceName();
}
