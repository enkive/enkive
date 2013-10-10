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
package com.linuxbox.enkive.administration;

import com.linuxbox.util.Version;
import com.linuxbox.util.Version.VersionException;


/**
 * Interface for the administration tasks
 * @author dang
 *
 */
public interface AdministrationService {

	/**
	 * Get the UID for this instance of Enkive.  It is created if it does not exist.
	 * @return UID of instance.
	 */
	String getUID();
	
	/**
	 * Set the UID for this instance of Enkive.
	 */
	void setUID(String UID);

	/**
	 * Check to see if there is a newer version of Enkive to update to.  Throws a
	 * VersionException if the check could not be made (e.g. no connection to enkive.org).
	 * @return A version, if there is a newer version, or null if up-to-date
	 */
	Version updateCheck() throws VersionException;
	
	/**
	 * Get the most recently checked version from local storage.
	 * 
	 * @return Most recent version if any, or null if no check has ever been made
	 */
	Version getVersion();
}
