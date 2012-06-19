/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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

public class LockAcquisitionException extends LockServiceException {
	private static final long serialVersionUID = 8481271470965024607L;

	public LockAcquisitionException(String identifier, String message) {
		super(describe(identifier, message));
	}

	public LockAcquisitionException(String identifier, Throwable e) {
		super(describe(identifier, null), e);
	}

	private static String describe(String identifier, String message) {
		final String lockInfo = "lock: \"" + identifier + "\"";
		if (message == null) {
			return lockInfo;
		} else {
			return message + "; " + lockInfo;
		}
	}
}
