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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.util.StringUtils;

public abstract class AbstractMessageSummary implements MessageSummary {
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"EEE, dd MMM yyyy HH:mm:ss ZZZZ");
	protected static final Pattern extractEmailAddressRE = Pattern
			.compile("<(.*?)>");

	protected String id;
	protected String messageId;
	protected String mailFrom;
	protected List<String> rcptTo;
	protected String from;
	protected List<String> to;
	protected List<String> cc;
	protected Date date;
	protected String subject;

	public AbstractMessageSummary() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	@Override
	public void appendRcptTo(String rcptTo) {
		this.rcptTo.add(rcptTo);
	}

	public String getMailFrom() {
		return mailFrom;
	}

	@Override
	public List<String> getRcptTo() {
		return rcptTo;
	}

	@Override
	public String getMessageId() {
		return messageId;
	}

	@Override
	public String getCleanMessageId() {
		StringBuffer sb = new StringBuffer(messageId);

		if (sb.length() > 0 && sb.charAt(0) == '<') {
			sb.deleteCharAt(0);
		}

		int lastIndex = sb.length() - 1;
		if (sb.length() > 0 && sb.charAt(lastIndex) == '>') {
			sb.deleteCharAt(lastIndex);
		}

		return sb.toString();
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getDateStr() {
		return dateFormatter.format(date);
	}

	@Override
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public void setRcptTo(List<String> rcptTo) {
		this.rcptTo = rcptTo;
	}

	@Override
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String getNormalizedMailFrom() throws BadMessageException {
		return extractEmailAddress(getMailFrom());
	}

	@Override
	public Set<String> getNormalizedRcptTo() throws BadMessageException {
		Set<String> result = new HashSet<String>(getRcptTo().size());
		for (String address : getRcptTo()) {
			result.add(extractEmailAddress(address));
		}
		return result;
	}

	@Override
	public String getFrom() {
		return from;
	}

	@Override
	public List<String> getTo() {
		return to;
	}

	@Override
	public List<String> getCc() {
		return cc;
	}

	@Override
	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	@Override
	public void appendCc(String cc) {
		this.cc.add(cc);
	}

	@Override
	public void setFrom(String from) {
		this.from = from;
	}

	@Override
	public void setTo(List<String> to) {
		this.to = to;
	}

	@Override
	public void appendTo(String to) {
		this.to.add(to);
	}

	@Override
	public List<String> getBcc() {
		List<String> recipients = new LinkedList<String>(getRcptTo());
		recipients.removeAll(getTo());
		recipients.removeAll(getCc());
		return recipients;
	}

	public String getToStr() {
		return StringUtils.collectionToCommaSeparatedString(getTo());
	}

	public String getCcStr() {
		return StringUtils.collectionToCommaSeparatedString(getCc());
	}

	public String getBccStr() {
		return StringUtils.collectionToCommaSeparatedString(getBcc());
	}

	protected String extractEmailAddress(String addressHeader)
			throws BadMessageException {
		Matcher matcher = extractEmailAddressRE.matcher(addressHeader);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			throw new BadMessageException(
					"could not extract SMTP header email address");
		}
	}
}