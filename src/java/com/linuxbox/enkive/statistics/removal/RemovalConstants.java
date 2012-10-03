/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
 ******************************************************************************/
package com.linuxbox.enkive.statistics.removal;

public interface RemovalConstants {
	public static String METHOD = "cleanAll";

	// values assigned by how long you want to keep them (eg. 48 in the hour id
	// means it is kept for 48 hours, 60 day means keep for 60 days)
	public static int REMOVAL_RAW_ID = 2;
	public static int REMOVAL_HOUR_ID = 48;
	public static int REMOVAL_DAY_ID = 60;
	public static int REMOVAL_WEEK_ID = 10;
	public static int REMOVAL_MONTH_ID = 24;
}
