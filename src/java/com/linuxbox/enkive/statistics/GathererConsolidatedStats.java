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
//TODO
package com.linuxbox.enkive.statistics;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.statistics.gathering.GathererAttributes;

public class GathererConsolidatedStats {
	abstract class ConsolidatedStats {
		protected String name; // key name
		protected Map<String, Double> summaries; // e.g., "min", "max", ec.

		public abstract boolean isSingleStats();
	}

	class SingleConsolidatedStats extends ConsolidatedStats {
		public SingleConsolidatedStats(String name,
				Map<String, Double> summaries) {
			this.name = name;
			this.summaries = summaries;
		}

		@Override
		public boolean isSingleStats() {
			return true;
		}
	}

	class MultiConsolidatedStats extends ConsolidatedStats {
		protected Map<String, ConsolidatedStats> next = new HashMap<String, ConsolidatedStats>();

		@SuppressWarnings("unchecked")
		public MultiConsolidatedStats(List<String> path,
				Map<String, Object> dbMap) {
			String name = path.get(0);
			if (path.size() != 1) {
				path.remove(path.size() - 1);
				next.put(name, new MultiConsolidatedStats(path,
						(Map<String, Object>) dbMap.get(name)));
			} else {
				next.put(name, new SingleConsolidatedStats(name,
						(Map<String, Double>) dbMap.get(name)));
			}
		}

		@Override
		public boolean isSingleStats() {
			return false;
		}
	}

	String gatherer;
	Date startTime; // or should it be Calendar if we have time zone info?
	Date endTime;
	GathererAttributes attributes;
	Map<String, MultiConsolidatedStats> stats;// string is key of name in
												// consolidatedStats

	public GathererConsolidatedStats(String gatherer, Date startTime,
			Date endTime, GathererAttributes attributes,
			Map<String, Object> dbMap) {
		this.gatherer = gatherer;
		this.startTime = startTime;
		this.endTime = endTime;
		this.attributes = attributes;
		setStats(dbMap);
	}

	private void setStats(Map<String, Object> dbMap) {
	}
}
