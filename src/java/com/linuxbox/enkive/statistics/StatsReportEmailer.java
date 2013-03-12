/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
 *******************************************************************************/
package com.linuxbox.enkive.statistics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.StatsRetrievalException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class StatsReportEmailer {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	String from;
	StatsGathererService gatherer;
	String mailHost;
	String to;

	public StatsReportEmailer(StatsGathererService gather) {
		this.gatherer = gather;
	}

	protected String buildReport() {
		try {
			return buildReportWithTemplate();
		} catch (Exception e) {
			LOGGER.warn("Error building statistics report email", e);
		}
		return "There was an error generating the Enkive statistics report";
	}

	private String buildReportWithTemplate() throws IOException,
			TemplateException, URISyntaxException, GathererException,
			StatsRetrievalException, ParseException, SchedulerException {
		Configuration cfg = new Configuration();
		File templatesDirectory = new File("config/templates");
		cfg.setDirectoryForTemplateLoading(templatesDirectory);

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("date", new Date());
		// TODO: Can this build a report from the previous day or time period
		// rather than gather on demand?
		List<RawStats> statistics = gatherer.gatherStats();

		for (RawStats rawStats : statistics) {
			Map<String, Object> statsMap = rawStats.toMap();
			root.put(rawStats.getGathererName(), statsMap);
		}
		Template temp = cfg.getTemplate("StatisticsEmailTemplate.ftl");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(os);
		temp.process(root, out);
		out.flush();
		return os.toString();
	}

	public String getFrom() {
		return from;
	}

	public String getMailHost() {
		return mailHost;
	}

	public String getTo() {
		return to;
	}

	public void sendReport() {

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", mailHost);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try { // Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			for (String toAddress : to.split(";")) {
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(toAddress));
			}

			// Set Subject: header field
			message.setSubject("Enkive Status Report");

			// Now set the actual message
			message.setText(buildReport());

			// Send message
			Transport.send(message);
		} catch (MessagingException mex) {
			LOGGER.warn("Error sending statistics report email", mex);
		}
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public void setTo(String to) {
		this.to = to;
	}
}
