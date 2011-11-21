package com.linuxbox.enkive.audit.mongodb;

import java.util.Date;
import java.util.List;

import com.linuxbox.enkive.audit.AuditEntry;
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
	public AuditEntry getEvent(String identifier) {
		if (permService.isAdmin())
			return super.getEvent(identifier);
		else
			return null;
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startTime, Date endTime) {
		if (permService.isAdmin())
			return super.search(eventCode, userIdentifier, startTime, endTime);
		else
			return null;
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip) {
		if (permService.isAdmin())
			return super.getMostRecentByPage(perPage, pagesToSkip);
		else
			return null;
	}

	@Override
	public long getAuditEntryCount() {
		if (permService.isAdmin())
			return super.getAuditEntryCount();
		else
			return 0;
	}

}
