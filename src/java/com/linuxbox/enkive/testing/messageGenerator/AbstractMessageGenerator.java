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
	
	protected String generateFrom(){
		return "enkive@enkive.com";
	}

	protected String generateTo(){
		return "enkive@enkive.com";
	}
	
	protected String generateSubject(){
		return "Enkive Test Message";
	}
	
	protected Date generateDate(){
		return new Date();
	}
	
	protected abstract String generateMessageBody();

}
