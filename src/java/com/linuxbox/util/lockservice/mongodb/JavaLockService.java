/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.util.lockservice.mongodb;

import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.linuxbox.util.lockservice.AbstractRetryingLockService;
import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockReleaseException;

public class JavaLockService extends AbstractRetryingLockService {
	private final ReentrantLock mutex;
	Date timestamp;
	Object note;

	public JavaLockService() {
		mutex = new ReentrantLock();
	}

	public void startup() {
	}

	public void shutdown() {
	}

	@Override
	public void lockWithRetries(String identifier, Object notation, int retries,
			long delayInMilliseconds) throws LockAcquisitionException {
		lock(identifier, notation);
	}

	@Override
	public LockRequestFailure lockWithFailureData(String identifier,
			String notation) throws LockAcquisitionException {

		if (mutex.isLocked()) {
			return new LockRequestFailure(identifier, timestamp, note);
		}
		lock(identifier, notation);
		return null;
	}

	/**
	 * Request sole access to a lock.
	 *
	 * This implementation only provides a single re-entrant lock, as opposed to a lock-per-ID.
	 *
	 * @param identifier	ID to lock
	 * @param notation		Note to attach to lock
	 */
	@Override
	public void lock(String identifier, Object notation)
			throws LockAcquisitionException {
		try {
			mutex.lockInterruptibly();
			timestamp = new Date();
			note = notation;
			return;
		} catch (InterruptedException e) {
			throw new LockAcquisitionException(identifier, e);
		}
	}

	/**
	 * Release a lock.
	 *
	 * @param identifier
	 * @throws LockReleaseException
	 */
	public void releaseLock(String identifier) throws LockReleaseException {
		try {
			mutex.unlock();
			timestamp = null;
			note = null;
		} catch (Exception e) {
			throw new LockReleaseException("Failed to release lock " + e);
		}
	}
}
