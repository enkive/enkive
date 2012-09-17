package com.linuxbox.enkive.statistics.services;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;

public class StatsGathererService extends VarsMaker {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.services");

	protected Map<String, GathererInterface> statsGatherers = null;

	// needs to maintain that key is the name in attributes!
	public StatsGathererService(Map<String, GathererInterface> statsGatherers)
			throws ParseException {
		this.statsGatherers = new HashMap<String, GathererInterface>();
		for (String key : statsGatherers.keySet()) {
			GathererInterface gatherer = statsGatherers.get(key);
			this.statsGatherers.put(gatherer.getAttributes().getName(),
					gatherer);
		}
	}

	public StatsGathererService(Set<GathererInterface> statsGatherers)
			throws ParseException {
		this.statsGatherers = new HashMap<String, GathererInterface>();
		for (GathererInterface gatherer : statsGatherers) {
			this.statsGatherers.put(gatherer.getAttributes().getName(),
					gatherer);
		}
	}

	public StatsGathererService(GathererInterface gatherer)
			throws ParseException {
		statsGatherers = new HashMap<String, GathererInterface>();
		statsGatherers.put(gatherer.getAttributes().getName(), gatherer);
	}

	/**
	 * adds the arg to the known gatherer list if it is not null
	 * @param gatherer - gatherer to add
	 */
	public void addGatherer(GathererInterface gatherer) {
		String name = gatherer.getAttributes().getName();
		if (statsGatherers != null) {
			statsGatherers.put(name, gatherer);
		} else {
			statsGatherers = new HashMap<String, GathererInterface>();
			statsGatherers.put(name, gatherer);
		}
	}

	/**
	 * returns every statistic from every known gatherer
	 * @return returns a set corresponding to every statistic avaiable to gather
	 * @throws ParseException
	 * @throws GathererException
	 */
	public List<RawStats> gatherStats() throws ParseException,
			GathererException {
		return gatherStats(null);
	}

	/**
	 * gathers statistics filtered by the map argument--returns all stats if map is null
	 * @param gathererKeys - map corresponding to the keys to be returned
	 * Format: {gathererName:[key1, key2, key3,...], ...}
	 * @return filtered stats
	 * @throws ParseException
	 * @throws GathererException
	 */
	public List<RawStats> gatherStats(
			Map<String, String[]> gathererKeys) throws
			GathererException {
		if (statsGatherers == null) {
			LOGGER.error("statsGatherers is null");
			return null;
		}

		if (statsGatherers.isEmpty()) {
			LOGGER.error("statsGatherers is empty");
			return null;
		}

		if (gathererKeys == null) {
			LOGGER.error("gathererKeys is null");
			return null;
		} 
		
		if (gathererKeys.isEmpty()){
			LOGGER.error("gathererKeys is empty");
			return null;
		}

		List<RawStats> statsList = createListOfRawStats();
		
		//TODO fix this to do point & interval 
		for (String statName: gathererKeys.keySet()) {
			RawStats gathererData = statsGatherers.get(statName)
					.getStatistics(gathererKeys.get(statName),gathererKeys.get(statName));
			
			Map<String, Object> result = gathererData.toMap();
			result.put(STAT_GATHERER_NAME, statsGatherers.get(statName)
					.getAttributes().getName());
			statsList.add(gathererData);
		}

		return statsList;
	}

	/**
	 * @return returns the map of all known gatherers
	 */
	public Map<String, GathererInterface> getStatsGatherers() {
		return statsGatherers;
	}

	/**
	 * takes a gathererName and returns the gatherer cooresponding to it
	 * @param name a gathererName (should be from attributes class)
	 * @return returns the gatherer cooresponding to the param
	 */
	public Map<String, GathererInterface> getStatsGatherers(String name) {
		Map<String, GathererInterface> gathererMap = new HashMap<String, GathererInterface>();
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
	 * @param name - a gatherer name (should be from attributes class)
	 */
	public void removeGatherer(String name) {
		if (statsGatherers.containsKey(name)) {
			statsGatherers.remove(name);
		}
	}
}
