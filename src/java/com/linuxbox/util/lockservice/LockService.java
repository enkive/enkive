package com.linuxbox.util.lockservice;

import com.linuxbox.util.lockservice.mongodb.MongoLockService.LockRequestFailure;

public interface LockService {
	void startup() throws LockServiceException;

	void shutdown() throws LockServiceException;

	LockRequestFailure lockWithFailureData(String identifier, String notation)
			throws LockAcquisitionException;

	boolean lock(String identifier, Object notation)
			throws LockAcquisitionException;

	boolean lockWithRetries(String identifier, Object notation, int retries,
			long delayInMilliseconds) throws LockAcquisitionException;

	void releaseLock(String identifier) throws LockReleaseException;
}
