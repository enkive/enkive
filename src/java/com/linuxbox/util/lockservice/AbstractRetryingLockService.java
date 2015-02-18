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
package com.linuxbox.util.lockservice;

public abstract class AbstractRetryingLockService implements LockService {
	@Override
	public void lockWithRetries(String identifier, Object notation,
			int retries, long delayInMilliseconds)
			throws LockAcquisitionException {
		LockAcquisitionException lastException = null;
		for (int i = 0; i < retries; i++) {
			lastException = null;
			try {
				lock(identifier, notation);
				return;
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
			throw new LockAcquisitionException(identifier, "Failet do get lock after "
					+ retries + " attempts.");
		}
	}
}
