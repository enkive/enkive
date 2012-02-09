package com.linuxbox.enkive;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.linuxbox.enkive.audit.AuditService;

public class EnkiveDaemon implements Daemon {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive");
	
	private static final String USER = AuditService.USER_SYSTEM;
	private static final String DESCRIPTION = "com.linuxbox.enkive.daemon";

	static final String[] CONFIG_FILES = { "jetty-server-webapps.xml" };

	protected String[] configFiles;
	protected AbstractApplicationContext appContext;
	protected AuditService auditService;

	@Override
	public void init(DaemonContext context) throws DaemonInitException,
			Exception {

		this.configFiles = CONFIG_FILES;
	}

	@Override
	public void start() throws Exception {
		appContext = new ClassPathXmlApplicationContext(configFiles);
		auditService = appContext
				.getBean("AuditLogService", AuditService.class);
		auditService.addEvent(AuditService.SYSTEM_STARTUP, USER, DESCRIPTION);

	}

	@Override
	public void stop() throws Exception {
		auditService.addEvent(AuditService.SYSTEM_SHUTDOWN, USER, DESCRIPTION);
		appContext.close();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Main");

	}

}
