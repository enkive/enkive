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
package com.linuxbox.enkive.statistics.services.retrieval;

import java.util.Date;
import java.util.Map;

/**
 * Describes a query for the stats database. If specified, will return entries
 * at or later than the start time stamp and less than (not less than or equal
 * to) the end time stamp. If either time stamp is null, then the query will not
 * consider it.
 * 
 * @author eric
 * 
 */
public abstract class StatsQuery {
	public Integer grainType;
	public Date startTimestamp = null;
	public Date endTimestamp = null;

	public abstract Map<String, Object> getQuery();
}
