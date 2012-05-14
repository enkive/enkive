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
