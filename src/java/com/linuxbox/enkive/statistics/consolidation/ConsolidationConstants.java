/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.statistics.consolidation;

public class ConsolidationConstants {

	public static int CONSOLIDATION_RAW = 0;
	public static int CONSOLIDATION_HOUR = 1;
	public static int CONSOLIDATION_DAY = 1 * 24;
	public static int CONSOLIDATION_WEEK = 1 * 24 * 7;
	public static int CONSOLIDATION_MONTH = 1 * 24 * 30;

	public static String CONSOLIDATION_AVG = "avg";
	public static String CONSOLIDATION_MAX = "max";
	public static String CONSOLIDATION_MIN = "min";
	public static String CONSOLIDATION_SUM = "sum";
	public static String CONSOLIDATION_TYPE = "gTyp";
	public static String CONSOLIDATION_WEIGHT = "wgt";
	public static String CONSOLIDATION_STD_DEV = "std";
}
