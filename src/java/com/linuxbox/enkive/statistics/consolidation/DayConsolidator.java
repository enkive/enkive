/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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

import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_DAY;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_HOUR;

import java.util.Calendar;
import java.util.Date;

import com.linuxbox.enkive.statistics.services.StatsClient;

public class DayConsolidator extends EmbeddedConsolidator {

	public DayConsolidator(StatsClient client) {
		super(client);
	}

	@Override
	public void setDates() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		Date upperDate = cal.getTime();
		cal.add(Calendar.DATE, -1);
		Date lowerDate = cal.getTime();
		setDates(upperDate, lowerDate);
	}

	@Override
	public void setTypes() {
		setTypes(CONSOLIDATION_DAY, CONSOLIDATION_HOUR);
	}
}
