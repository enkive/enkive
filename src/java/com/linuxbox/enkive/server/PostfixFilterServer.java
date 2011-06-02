/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.server;

import java.net.Socket;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.server.config.ThreadPoolServerConfiguration;
import com.linuxbox.enkive.mailprocessor.ArchivingProcessor;
import com.linuxbox.enkive.mailprocessor.processors.PostfixFilterProcessor;

/**
 * 
 * @author eric
 * 
 */
public class PostfixFilterServer extends ArchivingThreadPoolServer implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext = null;
	
	public PostfixFilterServer(int port, 
			ThreadPoolServerConfiguration threadConfiguration) {
		super("postfix_filter_server", port, threadConfiguration);
	}

	protected ArchivingProcessor createArchivingProcessor() {
		return (ArchivingProcessor) applicationContext.getBean("PostfixFilterProcessor");
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		PostfixFilterServer.applicationContext = ctx;
		
	}
}
