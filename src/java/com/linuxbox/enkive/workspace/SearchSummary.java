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
package com.linuxbox.enkive.workspace;

import java.util.Date;

public class SearchSummary {
	public String messageId;
	public String sender;
	public String recipient;
	public Date dateEarliest;
	public Date dateLatest;
	public String subject;
	public String content;

	public SearchSummary(String messageId, String sender, String recipient,
			Date dateEarliest, Date dateLatest, String subject, String content) {
		this.messageId = messageId;
		this.sender = sender;
		this.recipient = recipient;
		this.dateEarliest = dateEarliest;
		this.dateLatest = dateLatest;
		this.subject = subject;
		this.content = content;
	}
}
