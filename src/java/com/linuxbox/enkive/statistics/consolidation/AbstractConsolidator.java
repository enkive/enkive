package com.linuxbox.enkive.statistics.consolidation;

import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_SUM;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.services.StatsClient;

@SuppressWarnings("unchecked")
public abstract class AbstractConsolidator implements Consolidator {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Date endDate;
	protected Integer filterType;
	protected int consolidationType;
	protected boolean isEmbedded;
	protected Date startDate;

	public AbstractConsolidator(StatsClient client) {
		this.client = client;
//		setDates();
		setTypes();
	}

	public void storeConsolidatedData() {
		client.storeData(consolidateData());
	}

	/**
	 * Builds a map that cooresponds to the consolidation methods
	 * @param method - the method to use
	 * @param exampleData - an example data object (for type consistancy after consolidation)
	 * @param statsMaker - the pre-populated DescriptiveStatstistics object to pull stats from
	 * @param statData - the map to populate with consolidated data
	 */
	public void methodMapBuilder(String method, DescriptiveStatistics statsMaker,
			Map<String, Object> statData) {
		if (method.equals(CONSOLIDATION_SUM)) {
			statData.put(method, statsMaker.getSum());
		} else if (method.equals(CONSOLIDATION_MAX)) {
			statData.put(method, statsMaker.getMax());
		} else if (method.equals(CONSOLIDATION_MIN)) {
			statData.put(method, statsMaker.getMin());
		} else if (method.equals(CONSOLIDATION_AVG)) {
			statData.put(method, statsMaker.getMean());
		} 
	}
	
	@Override
	public abstract List<Map<String, Object>> consolidateData();

	/** this method recurses through a given template map to add consolidated data to a new map
	 * as defined by each key's ConsolidationDefinition
	 * @param templateData - the map used to trace
	 * @param consolidatedMap - the map being built
	 * @param path - the path to variables being used for trace
	 * @param statKeys - the list of a gatherer's consolidation definitions
	 * @param gathererData- all the data cooresponding to a given gatherer
	 * @return returns the built consolidatedMap variable
	 */
	protected Map<String, Object> generateConsolidatedMap(
			Map<String, Object> templateData,
			Map<String, Object> consolidatedMap, LinkedList<String> path,
			List<ConsolidationKeyHandler> statKeys, List<Map<String, Object>> gathererData) {
		for (String key : templateData.keySet()) {
			path.addLast(key);
			ConsolidationKeyHandler matchingDef = findMatchingPath(path, statKeys);
			if (matchingDef != null) {
				consolidateMaps(consolidatedMap, gathererData, matchingDef,
						path);
			} else {
				if (templateData.get(key) instanceof Map) {
					generateConsolidatedMap(
							(Map<String, Object>) templateData.get(key),
							consolidatedMap, path, statKeys, gathererData);
				}
			}
			path.removeLast();

		}
		return consolidatedMap;
	}

	/**
	 * this method takes a map and a path and uses the path to trace down the map to a data object
	 * it then returns that data
	 * @param dataMap - the map the data will be extracted from
	 * @param path - the path to traced on
	 * @return the found data object or null if path does not work
	 */
	protected Object getDataVal(Map<String, Object> dataMap, List<String> path) {
		Object map = dataMap;
		for (String key : path) {
			if (((Map<String, Object>) map).containsKey(key)) {
				if (((Map<String, Object>) map).get(key) instanceof Map) {
					map = ((Map<String, Object>) map).get(key);
				} else {
					return ((Map<String, Object>) map).get(key);
				}
			}
		}
		return null;
	}

	/** determines if a path matches any of the ConsolidationDefinitions for a given gatherer
	 * it does this by comparing each of the path's strings to each of the definition's strings
	 * asterisks are considered 'any' and are skipped
	 * @param path - the path to be checked
	 * @param keys - the gatherer's consolidation definitions
	 * @return if it finds a matching path it returns the corresponding ConsolidationDefinition
	 * if not it returns null
	 */
	private ConsolidationKeyHandler findMatchingPath(List<String> path, List<ConsolidationKeyHandler> keys) {
		for (ConsolidationKeyHandler def : keys) {// get one key definition
			if (def.getMethods() == null) {
				continue;
			}
			boolean isMatch = true;
			int pathIndex = 0;
			int defIndex = 0;
			String keyStr;
			String pathStr;
			List<String> keyString = def.getKey();
			if (keyString.size() > path.size()) {
				isMatch = false;
				continue;
			}

			while (pathIndex < path.size()) {
				if (defIndex >= keyString.size()) {
					isMatch = false;
					break;
				}

				while (keyString.get(defIndex).equals("*")
						&& defIndex < keyString.size()) {
					if (defIndex == keyString.size() - 1) {
						if (keyString.get(defIndex).equals("*")) {
							if (defIndex == path.size() - 1) {
								return def;
							} else {
								isMatch = false;
								break;
							}
						}
					} else {
						defIndex++;
						pathIndex++;
						keyStr = keyString.get(defIndex);
						pathStr = path.get(pathIndex);
					}
				}
				if (pathIndex >= path.size()) {
					isMatch = false;
					break;
				}

				keyStr = keyString.get(defIndex);
				pathStr = path.get(pathIndex);

				if (keyStr.equals(pathStr)) {
					pathIndex++;
					defIndex++;
				} else {
					isMatch = false;
					break;
				}
			}
			if (isMatch) {
				return def;
			}
		}
		return null;
	}

	/** this method takes a data object and inserts it at the end of a path on a given map
	 * @param path - the path to traverse
	 * @param statsData - the map to insert data into
	 * @param dataToAdd - the data to insert
	 */
	protected void putOnPath(List<String> path, Map<String, Object> statsData,
			Map<String, Object> dataToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, dataToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				} else {
					// TODO create the missing intervening maps
					LOGGER.error("Path does not exist");
					break;
				}
			}
			index++;
		}
	}
	
	public abstract List<List<Map<String, Object>>> gathererFilter(String gathererName);

	/**
	 * converts a statistic object into a double
	 * @param stat the statistic object to convert
	 * @return a double representing the statistic object
	 */
	protected double statToDouble(Object stat) {
		double input = -1;
		if (stat instanceof Integer) {
			input = (double) ((Integer) stat).intValue();
		} else if (stat instanceof Long) {
			input = (double) ((Long) stat).longValue();
		} else if (stat instanceof Double) {
			input = ((Double) stat).doubleValue();
		} else if (stat instanceof Date) {
			input = (double) ((Long) ((Date) stat).getTime()).longValue();
		} else {
			LOGGER.warn("statToDouble(Object stat)-unexpected Object type");
		}
		return input;
	}

	public abstract void setDates();
	
	public void setDates(Date upperDate, Date lowerDate){
		this.startDate = lowerDate;
		this.endDate = upperDate;
	}
	
	public abstract void setTypes();
	
	public void setTypes(int consolidationType, Integer filterType){
		this.consolidationType = consolidationType;
		this.filterType = filterType;
	}

	/**
	 * This method gets consolidated data from the service data and inserts it into
	 * the consolidateData map argument.
	 * 
	 * @param consolidatedData map to insert consolidated Data into (must have valid dataPath)
	 * if the path has data at the end it will be overwritten
	 * @param serviceData - all data relating to a service
	 * @param keyDef - defines the consolidation methods to use on the serviceData
	 * @param dataPath - the path on which to store the data in the consolidatedMap
	 */
	protected abstract void consolidateMaps(
			Map<String, Object> consolidatedData,
			List<Map<String, Object>> serviceData, ConsolidationKeyHandler keyDef,
			LinkedList<String> dataPath);
}
