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
package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;

import java.util.List;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;

public class GathererAttributes {
	protected List<ConsolidationKeyHandler> keys;
	protected String serviceName;
	protected String humanName;

	public GathererAttributes(String serviceName, String humanName,
			List<ConsolidationKeyHandler> keys) {
		this.humanName = humanName;
		this.serviceName = serviceName;
		this.keys = keys;

		// serviceName and Timestamp must always be specified
		keys.add(new ConsolidationKeyHandler(STAT_GATHERER_NAME
				+ "::Gatherer Name:"));
		keys.add(new ConsolidationKeyHandler(STAT_TIMESTAMP + "::Time Stamp:"));
	}

	/**
	 * @return the consolidation handlers cooresponding to this gatherer
	 */
	public List<ConsolidationKeyHandler> getKeys() {
		return keys;
	}

	/**
	 * @return the name of the gatherer this attributes class belongs to
	 */
	public String getName() {
		return serviceName;
	}

	/**
	 * @return the human-readable name of the gatherer this attributes class
	 *         belongs to
	 */
	public String getHumanName() {
		return humanName;
	}
}
