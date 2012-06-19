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
package com.linuxbox.enkive.audit;

import java.io.PrintWriter;

/**
 * This class keeps track of its own "true cause" since this can be thrown
 * during an archiving operation, which can be in a retrying transaction helper.
 * That helper will retry the transaction if the exception thrown belongs to a
 * given set or if anything in its cause chain belongs to that set. One item in
 * that set is a SQL exception, which could be in the cause chain of an
 * AuditTrailException.
 * 
 * @author eric
 * 
 */
public class AuditTrailException extends Exception {
	private static final long serialVersionUID = 1L;

	private Throwable trueCause;

	public AuditTrailException(String message) {
		super(message);
	}

	public AuditTrailException(String message, Throwable throwable) {
		super(message);
		trueCause = throwable;
	}

	public AuditTrailException(Throwable throwable) {
		super();
		trueCause = throwable;
	}

	@Override
	public void printStackTrace(PrintWriter w) {
		super.printStackTrace(w);
		if (trueCause != null) {
			w.println("Caused by:");
			trueCause.printStackTrace(w);
		}
	}
}
