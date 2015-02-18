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
package com.linuxbox.enkive.audit.mongodb;

import java.util.Date;
import java.util.List;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.MongoClient;

public class PermissionsEnforcingMongoAuditService extends MongoAuditService {

	PermissionService permService;

	public PermissionsEnforcingMongoAuditService(PermissionService permService,
			MongoClient mongo, String database, String collection) {
		super(mongo, database, collection);
		this.permService = permService;
	}
	
	public PermissionsEnforcingMongoAuditService(PermissionService permService,
			MongoDbInfo dbInfo) {
		super(dbInfo);
		this.permService = permService;
	}

	@Override
	public AuditEntry getEvent(String identifier) throws AuditServiceException {
		try {
			if (permService.isAdmin())
				return super.getEvent(identifier);
			else
				return null;
		} catch (CannotGetPermissionsException e) {
			throw new AuditServiceException(
					"Could not get permissions for user", e);
		}
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startTime, Date endTime) throws AuditServiceException {
		try {
			if (permService.isAdmin())
				return super.search(eventCode, userIdentifier, startTime,
						endTime);
			else
				return null;
		} catch (CannotGetPermissionsException e) {
			throw new AuditServiceException(
					"Could not get permissions for user " + userIdentifier, e);
		}
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditServiceException {
		try {
			if (permService.isAdmin())
				return super.getMostRecentByPage(perPage, pagesToSkip);
			else
				return null;
		} catch (CannotGetPermissionsException e) {
			throw new AuditServiceException(
					"Could not get permissions for user", e);
		}
	}

	@Override
	public long getAuditEntryCount() throws AuditServiceException {
		try {
			if (permService.isAdmin())
				return super.getAuditEntryCount();
			else
				return 0;
		} catch (CannotGetPermissionsException e) {
			throw new AuditServiceException(
					"Could not get permissions for user", e);
		}
	}

}
