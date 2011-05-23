package com.linuxbox.util.lockservice.mongodb;

import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockService;

public abstract class AbstractRetryingLockService implements LockService {
	@Override
	public boolean lockWithRetries(String identifier, Object notation,
			int retries, long delayInMilliseconds)
			throws LockAcquisitionException {
		LockAcquisitionException lastException = null;
		for (int i = 0; i < retries; i++) {
			lastException = null;
			try {
				boolean result = lock(identifier, notation);
				if (result) {
					return true;
				}
			} catch (LockAcquisitionException e) {
				lastException = e;
			}

			try {
				Thread.sleep(delayInMilliseconds);
			} catch (InterruptedException e) {
				throw new LockAcquisitionException(
						"thread interrupted while trying to acquire lock", e);
			}
		}

		if (lastException != null) {
			throw lastException;
		} else {
			return false;
		}
	}
}