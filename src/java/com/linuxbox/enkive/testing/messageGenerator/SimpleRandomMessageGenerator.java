package com.linuxbox.enkive.testing.messageGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class SimpleRandomMessageGenerator extends
		AbstractMessageGenerator {
	
	protected Random randGen;
	protected String messageBodyDataDirectory;

	public SimpleRandomMessageGenerator(String messageBodyDataDirectory) {
		super();
		randGen = new Random();
		this.messageBodyDataDirectory = messageBodyDataDirectory;
	}

	@Override
	protected String generateMessageBody() {
		InputStream is = null;
		StringBuffer messageBody = new StringBuffer();
		try {
			File messageBodyDataDir = new File(messageBodyDataDirectory);
			File[] files = messageBodyDataDir.listFiles();
	
			int fileToGet = randGen.nextInt(files.length);
			is = new FileInputStream(files[fileToGet]);
	
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));
			final String lineSeparator = System.getProperty("line.separator");
			String s;
			while ((s = dis.readLine()) != null) {
				messageBody.append(s + lineSeparator);
			}
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
	
			}
	
		}
		return messageBody.toString();
	}

}
