/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.message;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.linuxbox.enkive.exception.BadMessageException;

/**
 * Contains minimal information about a message, information that would likely
 * be displayed in a table of messages. The only question is whether to, cc, and
 * bcc should be here (if they are then so should from to be complete).
 * 
 * @author eric
 * 
 */
public interface MessageSummary {
	/**
	 * Returns some sort of identifier -- a record identifier/primary key on the
	 * back-end, most likely.
	 * 
	 * @return
	 */
	public String getId();

	public void setId(String id);

	/**
	 * 
	 * @return the data from the RCPT TO (S|L)MTP command TODO should this
	 *         return a set rather than a list? is order meaningful? are
	 *         duplicates allowed? the email address will be surrounded angle
	 *         brackets
	 */
	public abstract List<String> getRcptTo();

	/**
	 * 
	 * @return a cleaned up version for MAIL FROM; no angle brackets, no SIZE
	 *         indicator as per RFC 1870
	 */
	public abstract String getNormalizedMailFrom() throws BadMessageException;

	/**
	 * 
	 * @return a cleaned up version of RCPT TO, no angle brackets
	 */
	public abstract Set<String> getNormalizedRcptTo()
			throws BadMessageException;

	/**
	 * 
	 * @return the text after "Subject: " in the header
	 */
	public abstract String getSubject();

	/**
	 * 
	 * @return the date when the message was sent as indicated by the Date
	 *         header
	 */
	public abstract Date getDate();

	/**
	 * 
	 * @return the date as a string
	 */
	public abstract String getDateStr();

	/**
	 * 
	 * @return the text after "Message-ID: " in the header
	 */
	public abstract String getMessageId();

	/**
	 * 
	 * @return the text after "Message-ID: " in the header but without the angle
	 *         brackets surrounding it.
	 */
	public String getCleanMessageId();

	/**
	 * 
	 * Sets the MailFrom attribute
	 * 
	 * @param mailFrom
	 */
	public abstract void setMailFrom(String mailFrom);

	/**
	 * 
	 * Sets the rcptTo attribute
	 * 
	 * @param rcptList
	 */
	public abstract void setRcptTo(List<String> rcptTo);

	/**
	 * 
	 * Adds an address to the rcptTo attribute
	 * 
	 * @param rcptList
	 */
	public abstract void appendRcptTo(String rcptTo);

	/**
	 * 
	 * Sets the subject attribute
	 * 
	 * @param subject
	 */
	public abstract void setSubject(String subject);

	/**
	 * 
	 * Sets the date attribute
	 * 
	 * @param date
	 */
	public abstract void setDate(Date date);

	/**
	 * 
	 * Sets the messageId attribute
	 * 
	 * @param messageId
	 */
	public abstract void setMessageId(String messageId);

	/**
	 * 
	 * @return the data from the MAIL FROM (S|L)MTP command; the email address
	 *         will be surrounded angle brackets around the email address and
	 *         may contain a SIZE indicator as per RFC 1870
	 */
	public String getMailFrom();

	/**
	 * 
	 * @return the text after "From: " in the header
	 */
	public String getFrom();

	/**
	 * 
	 * @return the "From:" sender of the message, stripped of all brackets
	 */
	public String getFromStr();

	/**
	 * 
	 * @return a list containing all the elements of the "Cc" header, but
	 *         individually
	 */
	public List<String> getCc();

	/**
	 * 
	 * @return a list containing all the known "Bcc" recipients. This is done by
	 *         taking RcptTo recipients and "subtracting" the To and Cc
	 *         recipients.
	 */
	public List<String> getBcc();

	/**
	 * 
	 * @return the "Cc:" recipients of the message as a comma-separated string
	 */
	public String getCcStr();

	/**
	 * 
	 * @return the "Bcc:" recipients of the message as a comma-separated string
	 */
	public String getBccStr();

	/**
	 * 
	 * @return a list containing all the elements of the "To" header, but
	 *         individually
	 */
	public abstract List<String> getTo();

	/**
	 * 
	 * @return the "To:" recipients of the message as a comma-separated string
	 */
	public abstract String getToStr();

	/**
	 * 
	 * Sets the from attribute
	 * 
	 * @param from
	 */
	public void setFrom(String from);

	/**
	 * 
	 * Sets the to attribute
	 * 
	 * @param to
	 */
	public void setTo(List<String> to);

	/**
	 * 
	 * Appends to the to attribute
	 * 
	 * @param to
	 */
	public void appendTo(String to);

	/**
	 * 
	 * Sets the cc attribute
	 * 
	 * @param cc
	 */
	public void setCc(List<String> cc);

	/**
	 * 
	 * Appends to the cc attribute
	 * 
	 * @param cc
	 */
	public void appendCc(String cc);
}