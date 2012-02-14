/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.linuxbox.util;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MBeanUtils {
	private final static Log logger = LogFactory.getLog("com.linuxbox.util");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ObjectName registerMBean(Object mBean, Class mBeanInterface,
			String type, String name) {
		try {
			StandardMBean standardMBean = new StandardMBean(mBean,
					mBeanInterface);
			return registerMBean(standardMBean, type, name, null);
		} catch (NotCompliantMBeanException e) {
			logger.error("noncompliant mbean", e);
		}

		return null;
	}

	public static ObjectName registerMBean(Object mBean, String type,
			String name) {
		return registerMBean(mBean, type, name, null);
	}

	/**
	 * 
	 * @param mBean
	 * @param type
	 * @param name
	 * @param otherParams
	 *            comma-separated string containing additional parameters for
	 *            the MBean server
	 */
	public static ObjectName registerMBean(Object mBean, String type,
			String name, String otherParams) {
		try {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			ObjectName objName = new ObjectName("enkive:type=" + type
					+ ",name=" + name
					+ (otherParams == null ? "" : "," + otherParams));
			server.registerMBean(mBean, objName);
			return objName;
		} catch (InstanceAlreadyExistsException e) {
			logger.warn("tried to re-register mbean", e);
		} catch (MBeanRegistrationException e) {
			logger.error("could not register mbean", e);
		} catch (NotCompliantMBeanException e) {
			logger.error("noncompliant mbean", e);
		} catch (MalformedObjectNameException e) {
			logger.error("noncompliant mbean name", e);
		}

		return null;
	}

	public static void unregisterMBean(ObjectName name) {
		if (name == null) {
			return;
		}

		try {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			server.unregisterMBean(name);
		} catch (MBeanRegistrationException e) {
			logger.error("could not unregister mbean " + name, e);
		} catch (InstanceNotFoundException e) {
			logger.error("could not find mbean " + name, e);
		}
	}
}
