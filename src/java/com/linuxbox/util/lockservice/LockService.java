package com.linuxbox.util.lockservice;

import com.linuxbox.util.lockservice.mongodb.MongoLockService.LockRequestFailure;

public interface LockService {
	LockRequestFailure lockWithFailureData(String identifier, String notation)
			throws LockAcquisitionException;

	boolean lock(String identifier, String notation)
			throws LockAcquisitionException;

	void releaseLock(String identifier) throws LockReleaseException;
}
