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
package com.linuxbox.enkive.testing.messageGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.james.mime4j.dom.field.ContentTypeField;

public class RandomMessageGenerator extends AbstractMessageGenerator {

	public static String messageBodyDataDirectory = "/home/lee/Storage/Projects/Enkive-3/workspace/Enkive/test/data/gutenbergData";
	public static int DATE_RANGE = 365;

	protected Random randGen;

	public RandomMessageGenerator() {
		super();
		randGen = new Random();
	}

	@Override
	public MimeMessage generateMessage() {
		MimeMessage message = new MimeMessage(session);

		// Set From: header field of the header.
		try {
			message.setFrom(new InternetAddress(generateFrom()));

			// Set To: header field of the header.
			for (String toAddress : generateTo().split(";"))
				message.addRecipient(Message.RecipientType.TO,
						new InternetAddress(toAddress));

			// Set Subject: header field
			message.setSubject(generateSubject());
			message.setSentDate(generateDate());

			// Now set the actual message
			Multipart mp = new MimeMultipart();
			BodyPart bp = new MimeBodyPart();
			bp.setText(generateMessageBody());
			BodyPart nestedMessage = new MimeBodyPart();
			MimeMessage nestedMimeMessage = new SimpleRandomMessageGenerator()
					.generateMessage();
			nestedMimeMessage.setSentDate(message.getSentDate());
			nestedMessage.setContent(
					new SimpleRandomMessageGenerator().generateMessage(),
					ContentTypeField.TYPE_MESSAGE_RFC822);
			mp.addBodyPart(bp);
			mp.addBodyPart(nestedMessage);
			message.setContent(mp);

			return message;
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
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
			String s;
			while ((s = dis.readLine()) != null) {
				messageBody.append(s + System.getProperty("line.separator"));
			}

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

	@Override
	protected String generateFrom() {
		return "enkive@enkive.com";
	}

	@Override
	protected String generateTo() {
		return "enkive@enkive.com";
	}

	@Override
	protected String generateSubject() {
		return "Enkive Test Message 5";
	}

	@Override
	protected Date generateDate() {
		Calendar cal = Calendar.getInstance();
		int subDate = -1 * randGen.nextInt(DATE_RANGE);
		cal.add(Calendar.DATE, subDate);
		return cal.getTime();
	}

}
