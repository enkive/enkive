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
package com.linuxbox.util.lockservice;

public abstract class LockServiceException extends Exception {
	private static final long serialVersionUID = 8706352984347388472L;

	public LockServiceException() {
		super();
	}

	public LockServiceException(String message) {
		super(message);
	}

	public LockServiceException(Throwable thrown) {
		super(thrown);
	}

	public LockServiceException(String message, Throwable thrown) {
		super(message, thrown);
	}
}
