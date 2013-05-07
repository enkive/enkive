package com.linuxbox.enkive.audit;

import java.util.Date;
import java.util.List;

/**
 * Implements an audit service that does nothing. Means when we don't want
 * auditing we don't have to check for the existence of an audit service, we can
 * just send audit entries here and they'll be thrown away.
 */
public class NullAuditService implements AuditService {

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description) throws AuditServiceException {
	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description, boolean truncateDescription)
			throws AuditServiceException {
		// empty
	}

	@Override
	public AuditEntry getEvent(String identifier) throws AuditServiceException {
		return null;
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifier,
			Date startTime, Date endTime) throws AuditServiceException {
		return null;
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditServiceException {
		return null;
	}

	@Override
	public long getAuditEntryCount() throws AuditServiceException {
		return 0;
	}
}
