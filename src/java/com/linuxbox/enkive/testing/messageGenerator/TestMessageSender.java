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

import java.io.IOException;
import java.net.UnknownHostException;

import javax.mail.MessagingException;

import com.linuxbox.enkive.importer.AbstractMessageImporter;

public class TestMessageSender extends AbstractMessageImporter {

	MessageGenerator messageGenerator;

	TestMessageSender(String host, String port) throws UnknownHostException {
		super(host, port);
		messageGenerator = new RandomMessageGenerator();
	}

	public void sendGeneratedMessages() {
		try {
			setWriter();
			for (int i = 0; i < 100000; i++) {
				try {
					sendMessage(messageGenerator.generateMessage());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			closeWriter();
		}
	}

	public static void main(String args[]) throws UnknownHostException {
		TestMessageSender messageSender = new TestMessageSender("127.0.0.1",
				"2526");
		messageSender.sendGeneratedMessages();

	}

}
