package com.linuxbox.enkive.testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class AttachmentVerifier {
	protected final static int ACCESS_INCREMENT = 50;

	private final Mongo mongo;
	private final DB mongoDB;
	private final DBCollection auditCollection;

	Properties loadConfigProperties() throws IOException {
		Properties properties = new Properties();
		properties
				.load(new FileInputStream("config/default/enkive.properties"));
		properties.load(new FileInputStream("config/enkive.properties"));
		return properties;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Properties 

	}

}
