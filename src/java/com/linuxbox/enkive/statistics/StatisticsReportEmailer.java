package com.linuxbox.enkive.statistics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class StatisticsReportEmailer {

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics");

	StatisticsGatherer statsGatherer;
	String to;
	String from;
	String mailHost;

	public StatisticsReportEmailer(StatisticsGatherer statsGatherer) {
		this.statsGatherer = statsGatherer;
	}

	public void sendReport(Collection<String> addresses) {

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
			for (String toAddress : to.split(";"))
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(toAddress));

			// Set Subject: header field
			message.setSubject("Enkive Status Report");

			// Now set the actual message
			message.setText(buildReport());

			// Send message
			//Transport.send(message);
			System.out.println(buildReport());
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

	protected String buildReport() {
		try {
			return buildReportWithTemplate();
		} catch (Exception e) {
			LOGGER.warn("Error building statistics report email", e);
		}
		return "There was an error generating the Enkive statistics report";
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	private String buildReportWithTemplate() throws IOException,
			TemplateException, JSONException, URISyntaxException {
		Configuration cfg = new Configuration();
		File templatesDirectory = new File("config/templates");
		cfg.setDirectoryForTemplateLoading(templatesDirectory);

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("date", new Date());
		JSONObject statistics = statsGatherer.getStatisticsJSON();
		for (String serviceName : JSONObject.getNames(statistics)) {
			Map<String, String> service = new HashMap<String, String>();
			JSONArray serviceStatistics = statistics.getJSONArray(serviceName);

			for (int i = 0; i < serviceStatistics.length(); i++) {
				JSONObject statistic = serviceStatistics.getJSONObject(i);
				for (String statisticName : JSONObject.getNames(statistic)) {
					service.put(statisticName,
							statistic.getString(statisticName));
				}
			}
			root.put(serviceName, service);
		}
		// Create the hash for ``latestProduct''
		Template temp = cfg.getTemplate("StatisticsEmailTemplate.ftl");

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer out = new OutputStreamWriter(os);
		temp.process(root, out);
		out.flush();
		return os.toString();
	}

}
