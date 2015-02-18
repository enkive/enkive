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

import java.util.Date;

public interface LockService {
	public static class LockRequestFailure {
		public String identifier;
		public Date holderTimestamp;
		public Object holderNote;

		public LockRequestFailure(String identifier, Date holderTimestamp,
				Object holderNote) {
			this.identifier = identifier;
			this.holderTimestamp = holderTimestamp;
			this.holderNote = holderNote;
		}
	}

	void startup() throws LockServiceException;

	void shutdown() throws LockServiceException;

	LockRequestFailure lockWithFailureData(String identifier, String notation)
			throws LockAcquisitionException;

	void lock(String identifier, Object notation)
			throws LockAcquisitionException;

	void lockWithRetries(String identifier, Object notation, int retries,
			long delayInMilliseconds) throws LockAcquisitionException;

	void releaseLock(String identifier) throws LockReleaseException;
}
