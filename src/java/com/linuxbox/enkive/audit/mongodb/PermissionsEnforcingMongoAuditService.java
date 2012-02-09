package com.linuxbox.enkive.audit.mongodb;

import java.util.Date;
import java.util.List;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.exception.CannotGetPermissionsException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.mongodb.Mongo;

public class PermissionsEnforcingMongoAuditService extends MongoAuditService {

	PermissionService permService;

	public PermissionsEnforcingMongoAuditService(PermissionService permService,
			Mongo mongo, String database, String collection) {
		super(mongo, database, collection);
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
