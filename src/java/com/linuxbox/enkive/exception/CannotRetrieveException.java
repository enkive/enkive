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
package com.linuxbox.enkive.exception;

/**
 * FIXME: This exception is awfully generic. It's used for cases where something
 * is retrieved when there are insufficient permissions. It's also used for
 * other cases of failed retrieval. And that means we cannot return appropriate
 * error codes (e.g., HttpServletResponse.SC_UNAUTHORIZED or
 * HttpServletResponse.SC_INTERNAL_SERVER_ERROR). So perhaps we need to have
 * subclasses that address specific issues. And perhaps the error code should be
 * embedded in the resulting exception classes?
 */
public class CannotRetrieveException extends EnkiveException {
	private static final long serialVersionUID = 1L;

	public CannotRetrieveException() {
	}

	public CannotRetrieveException(String message) {
		super(message);
	}

	public CannotRetrieveException(Throwable cause) {
		super(cause);
	}

	public CannotRetrieveException(String message, Throwable cause) {
		super(message, cause);
	}
}
