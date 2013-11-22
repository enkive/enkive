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
package com.linuxbox.enkive.workspace.searchResult;


/**
 * This is thrown by getPage if there is a problem getting a page for a search.
 * It will contain an error message to be presented to the user.
 * @author dang
 */
public class ResultPageException extends Exception {
	private static final long serialVersionUID = 5479219772349290984L;

	public ResultPageException() {
		super();
	}
	public ResultPageException(String message) {
		super(message);
	}
	public ResultPageException(String message, Throwable t) {
		super(message, t);
	}

	public String toString() {
		String error = this.getLocalizedMessage();

		if (this.getCause() != null) {
			error += ": " + this.getCause().getLocalizedMessage();
		}

		error += " Please see the FAQ at http://enkive.org/faq";

		return error;
	}
}
