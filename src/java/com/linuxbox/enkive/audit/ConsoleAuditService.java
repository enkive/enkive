/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.audit;

import java.util.Date;
import java.util.List;

/**
 * This audit service just outputs audit entries to the console. It's primary
 * purpose is in testing.
 */
public class ConsoleAuditService implements AuditService {
	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description) {
		Date now = new Date();
		System.out.println("Audit Service: " + now + " ; " + eventCode + " ; "
				+ userIdentifier + " ; " + description);
	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description, boolean truncateDescription) {
		// for the console we don't care about truncating the description
		addEvent(eventCode, userIdentifier, description);
	}

	@Override
	public AuditEntry getEvent(String identifer) throws AuditServiceException {
		throw new AuditServiceException(
				"ConsoleAuditService cannot perform this action");
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifer,
			Date startDate, Date endDate) throws AuditServiceException {
		throw new AuditServiceException(
				"ConsoleAuditService cannot perform this action");
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int page)
			throws AuditServiceException {
		throw new AuditServiceException(
				"ConsoleAuditService cannot perform this action");
	}

	@Override
	public long getAuditEntryCount() throws AuditServiceException {
		return 0;
	}
}
