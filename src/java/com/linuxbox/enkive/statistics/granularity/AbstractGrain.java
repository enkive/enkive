package com.linuxbox.enkive.statistics.granularity;

import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_AVG;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_STD_DEV;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_SUM;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

@SuppressWarnings("unchecked")
public abstract class AbstractGrain implements Grain {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.granularity.AbstractGrain");
	protected StatsClient client;
	protected Date endDate;
	protected Integer filterType;
	protected int grainType;
	protected boolean isEmbedded;
	protected Date startDate;

	public AbstractGrain(StatsClient client) {
		this.client = client;
		setDates();
		setTypes();
	}

	public void storeConsolidatedData() {
		client.storeData(consolidateData());
	}

	public void methodMapBuilder(String method, Object exampleData,
			DescriptiveStatistics statsMaker, Map<String, Object> statData) {
		if (method.equals(GRAIN_SUM)) {
			statData.put(method, injectType(exampleData, statsMaker.getSum()));
		} else if (method.equals(GRAIN_MAX)) {
			statData.put(method, injectType(exampleData, statsMaker.getMax()));
		} else if (method.equals(GRAIN_MIN)) {
			statData.put(method, injectType(exampleData, statsMaker.getMin()));
		} else if (method.equals(GRAIN_AVG)) {
			statData.put(method, injectType(exampleData, statsMaker.getMean()));
		} else if (method.equals(GRAIN_STD_DEV)) {
			statData.put(method,
					injectType(exampleData, statsMaker.getStandardDeviation()));
		}
	}

	@Override
	public Set<Map<String, Object>> consolidateData() {
		Set<Map<String, Object>> storageData = new HashSet<Map<String, Object>>();
		for (GathererAttributes attribute : client.getAttributes()) {
			String name = attribute.getName();
			Set<Map<String, Object>> serviceData = gathererFilter(name);
			if (!serviceData.isEmpty()) {
				Map<String, Object> example = new HashMap<String, Object>(
						serviceData.iterator().next());
				Map<String, Object> mapToStore = new HashMap<String, Object>(
						example);
				generateConsolidatedMap(example, mapToStore,
						new LinkedList<String>(), attribute.getKeys(),
						serviceData);
				mapToStore.put(GRAIN_TYPE, grainType);
				if (mapToStore.containsKey("_id")) {
					mapToStore.remove("_id");
				}
				storageData.add(mapToStore);
			}
		}
		return storageData;
	}


	// NOAH: this method calls out for a nice JavaDoc comment
	//fixed
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
			List<KeyConsolidationHandler> statKeys, Set<Map<String, Object>> gathererData) {
		for (String key : templateData.keySet()) {
			path.addLast(key);
			KeyConsolidationHandler matchingDef = findMatchingPath(path, statKeys);
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

	protected int findWeight(Set<Map<String, Object>> serviceData) {
		return serviceData.size();
	}

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

	protected Object injectType(Object example, double value) {
		Object result = null;
		if (example instanceof Integer) {
			result = (int) value;
		} else if (example instanceof Long) {
			result = (long) value;
		} else if (example instanceof Double) {
			result = value;
		} else if (example instanceof Date) {
			result = new Date((long) value);
		}
		return result;
	}

	// NOAH: this method calls out for a nice JavaDoc comment
	//fixed
	/** determines if a path matches any of the ConsolidationDefinitions for a given gatherer
	 * it does this by comparing each of the path's strings to each of the definition's strings
	 * asterisks are considered 'any' and are skipped
	 * @param path - the path to be checked
	 * @param keys - the gatherer's consolidation definitions
	 * @return if it finds a matching path it returns the corresponding ConsolidationDefinition
	 * if not it returns null
	 */
	private KeyConsolidationHandler findMatchingPath(List<String> path, List<KeyConsolidationHandler> keys) {
		for (KeyConsolidationHandler def : keys) {// get one key definition
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

			while (pathIndex < path.size()) {// run through it to compare to
												// path
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

	// NOAH: this method calls out for a nice JavaDoc comment
	//fixed
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
					// TODO NOAH: is there a reason we don't create the missing
					// intervening maps?
					// Noah-No, but I'm not sure how.
					LOGGER.error("Cannot put data on path");
				}
			}
			index++;
		}
	}

	// NOAH: this method calls out for a nice JavaDoc comment
	//fixed (in interface)
	public Set<Map<String, Object>> gathererFilter(String name) {
		Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
		Map<String, Object> keyVals = new HashMap<String, Object>();
		keyVals.put(GRAIN_TYPE, filterType);
		query.put(name, keyVals);
		Set<Map<String, Object>> result = client.queryStatistics(query,
				startDate, endDate);
		return result;
	}

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
	
	public void setTypes(int grainType, Integer filterType){
		this.grainType = grainType;
		this.filterType = filterType;
	}

	protected abstract void consolidateMaps(
			Map<String, Object> consolidatedData,
			Set<Map<String, Object>> serviceData, KeyConsolidationHandler keyDef,
			LinkedList<String> dataPath);
}
