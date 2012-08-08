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

import java.io.IOException;
import java.net.UnknownHostException;

import javax.mail.MessagingException;

import com.linuxbox.enkive.importer.AbstractMessageImporter;

public class TestMessageSender extends AbstractMessageImporter {

	protected MessageGenerator messageGenerator;

	TestMessageSender(String host, String port, String messageBodyDirectory)
			throws UnknownHostException {
		super(host, port);
		messageGenerator = new RandomMessageGenerator(messageBodyDirectory);
	}

	public void sendGeneratedMessages(int numOfMessages) {
		try {
			setWriter();
			for (int i = 0; i < numOfMessages; i++) {
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
		if (args.length != 4) {
			System.err
					.println("Error: requires command-line arguments representing host, port number, path to message bodies, and number of messages to send");
			System.exit(1);
		}

		final String host = args[0];
		final String portString = args[1];
		final String path = args[2];
		final String numOfMessages = args[3];
		
		System.out.println(path);

		TestMessageSender messageSender = new TestMessageSender(host,
				portString, path);
		messageSender.sendGeneratedMessages(Integer.valueOf(numOfMessages));

	}

}
