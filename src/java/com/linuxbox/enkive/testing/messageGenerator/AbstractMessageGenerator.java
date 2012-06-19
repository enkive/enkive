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
 *******************************************************************************/
package com.linuxbox.enkive.testing.messageGenerator;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public abstract class AbstractMessageGenerator implements MessageGenerator {

	protected Session session;

	public AbstractMessageGenerator() {
		session = Session.getDefaultInstance(System.getProperties());
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
			message.setText(generateMessageBody());

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

	protected String generateFrom() {
		return "enkive@enkive.com";
	}

	protected String generateTo() {
		return "enkive@enkive.com";
	}

	protected String generateSubject() {
		return "Enkive Test Message";
	}

	protected Date generateDate() {
		return new Date();
	}

	protected abstract String generateMessageBody();

}
