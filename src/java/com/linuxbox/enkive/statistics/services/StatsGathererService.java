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
package com.linuxbox.enkive.statistics.services;

import static com.linuxbox.enkive.statistics.VarsMaker.createListOfRawStats;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.gathering.Gatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;

public class StatsGathererService {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services");

	protected Map<String, Gatherer> statsGatherers = null;

	// needs to maintain that key is the name in attributes!
	public StatsGathererService(Map<String, Gatherer> statsGatherers)
			throws ParseException {
		this.statsGatherers = new HashMap<String, Gatherer>();
		for (String key : statsGatherers.keySet()) {
			Gatherer gatherer = statsGatherers.get(key);
			this.statsGatherers.put(gatherer.getAttributes().getName(), gatherer);
		}
	}

	public StatsGathererService(Set<Gatherer> statsGatherers)
			throws ParseException {
		this.statsGatherers = new HashMap<String, Gatherer>();
		for (Gatherer gatherer : statsGatherers) {
			this.statsGatherers.put(gatherer.getAttributes().getName(),	gatherer);
		}
	}

	public StatsGathererService(Gatherer gatherer) throws ParseException {
		statsGatherers = new HashMap<String, Gatherer>();
		statsGatherers.put(gatherer.getAttributes().getName(), gatherer);
	}

	/**
	 * adds the arg to the known gatherer list if it is not null
	 * 
	 * @param gatherer
	 *            - gatherer to add
	 */
	public void addGatherer(Gatherer gatherer) {
		String name = gatherer.getAttributes().getName();
		if (statsGatherers != null) {
			statsGatherers.put(name, gatherer);
		} else {
			statsGatherers = new HashMap<String, Gatherer>();
			statsGatherers.put(name, gatherer);
		}
	}

	/**
	 * returns every statistic from every known gatherer
	 * 
	 * @return returns a set corresponding to every statistic avaiable to gather
	 * @throws ParseException
	 * @throws GathererException
	 */
	public List<RawStats> gatherStats() throws ParseException,
			GathererException {
		return gatherStats(null);
	}

	/**
	 * gathers statistics filtered by the map argument--returns all stats if map
	 * is null
	 * 
	 * @param gathererKeys
	 *            - map corresponding to the keys to be returned Format:
	 *            {gathererName:[key1, key2, key3,...], ...}
	 * @return filtered stats
	 * @throws ParseException
	 * @throws GathererException
	 */
	public List<RawStats> gatherStats(Map<String, List<String>> gathererKeys)
			throws GathererException {
		List<RawStats> statsList = createListOfRawStats();

		if (gathererKeys == null) {
			for (String gathererName : statsGatherers.keySet()) {
				Gatherer gatherer = statsGatherers.get(gathererName);
				RawStats stats = gatherer.getStatistics();
				stats.setGathererName(gathererName);
				statsList.add(stats);
			}
			return statsList;
		}

		if (statsGatherers == null || statsGatherers.isEmpty()) {
			LOGGER.error("statsGatherers is invalid");
			return null;
		}

		if (gathererKeys.isEmpty()) {
			LOGGER.error("gathererKeys is empty");
			return null;
		}

		for (String statName : gathererKeys.keySet()) {
			RawStats stats = statsGatherers.get(statName).getStatistics(
					gathererKeys.get(statName), gathererKeys.get(statName));
			stats.setGathererName(statsGatherers.get(statName).getAttributes()
					.getName());

			statsList.add(stats);
		}

		return statsList;
	}

	/**
	 * @return returns the map of all known gatherers
	 */
	public Map<String, Gatherer> getStatsGatherers() {
		return statsGatherers;
	}

	/**
	 * takes a gathererName and returns the gatherer cooresponding to it
	 * 
	 * @param name
	 *            a gathererName (should be from attributes class)
	 * @return returns the gatherer cooresponding to the param
	 */
	public Map<String, Gatherer> getStatsGatherers(String name) {
		Map<String, Gatherer> gathererMap = new HashMap<String, Gatherer>();
		gathererMap.put(name, statsGatherers.get(name));
		return gathererMap;
	}

	@PostConstruct
	public void init() {
		String info = "GathererService created with gatherers:";
		if (getStatsGatherers() != null) {
			for (String name : getStatsGatherers().keySet()) {
				info = info + " " + name;
			}
		}
		LOGGER.info(info);
	}

	/**
	 * remove a gatherer from the known gatherer map
	 * 
	 * @param name
	 *            - a gatherer name (should be from attributes class)
	 */
	public void removeGatherer(String name) {
		if (statsGatherers.containsKey(name)) {
			statsGatherers.remove(name);
		}
	}
}
