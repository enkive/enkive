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
package com.linuxbox.enkive.archiver.exceptions;

import com.linuxbox.enkive.exception.EnkiveException;

public class MessageArchivingServiceException extends EnkiveException {

	private static final long serialVersionUID = 1L;

	public MessageArchivingServiceException() {
		super();
	}

	public MessageArchivingServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageArchivingServiceException(String message) {
		super(message);
	}

	public MessageArchivingServiceException(Throwable cause) {
		super(cause);
	}
}
