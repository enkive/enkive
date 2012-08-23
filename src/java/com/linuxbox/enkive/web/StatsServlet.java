/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
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
package com.linuxbox.enkive.web;

import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.statistics.ConsolidationKeyHandler;
import com.linuxbox.enkive.statistics.RawStats;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.retrieval.StatsFilter;
import com.linuxbox.enkive.statistics.services.retrieval.StatsQuery;

@SuppressWarnings("unchecked")
public class StatsServlet extends EnkiveServlet {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.StatsServlet");
	private static final long serialVersionUID = 7062366416188559812L;

	private StatsClient client;

	/*
	 * Servlet Algorithm 
	 * 1. Get a DateRange for ts.min & ts.max 
	 * 1.5 if no dateRange is specified find instant data 
	 * 2. get allservice names 
	 * 3. loop over service names to build filter map 
	 * 4. while looping build a second map for query second map will only have
	 * serviceName, date range, and (optional) grainType 
	 * 5. query database using query map & filter map 
	 * 6. format query data 
	 * 7. return formatted data
	 * Note. handle errors with log messages & thrown exceptions
	 */

	private final String tsMax = STAT_TIMESTAMP + "." + GRAIN_MAX;
	private final String tsMin = STAT_TIMESTAMP + "." + GRAIN_MIN;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		client = getStatsClient();
	}

	/*
	 * JSON formatting algorithm 
	 * 1. get a map to use as template 
	 * 2. recurse template map until hit full path specified by attributes class 
	 * 3. when full path loop over entire data on that path to build a sorted set
	 * (sorted on ts.min) for each input to the set also include ts
	 * 4. when done with that loop store that set under the path on the
	 * consolidated data path 
	 * 5. return Set<Map<String,Object>> as json by list constructor
	 */
	private Map<String, Object> consolidateMaps(
			List<Map<String, Object>> serviceData, List<ConsolidationKeyHandler> statKeys) {
		if (serviceData.size() > 0) {
			Map<String, Object> template = new HashMap<String, Object>(
					serviceData.iterator().next());			
			LinkedList<String> path = new LinkedList<String>();
			Map<String, Object> result = new HashMap<String, Object>(template);
			consolidateMapsHelper(template, result, path, statKeys, serviceData);
			result.remove(STAT_GATHERER_NAME);
			result.remove(STAT_TIMESTAMP);
			result.remove("_id");
			return result;
		}
		return null;
	}

	private void consolidateMapsHelper(Map<String, Object> templateData,
			Map<String, Object> consolidatedMap, LinkedList<String> path,
			List<ConsolidationKeyHandler> statKeys, List<Map<String, Object>> serviceData) {
		for (String key : templateData.keySet()) {
			path.addLast(key);
			ConsolidationKeyHandler matchingConsolidationDefinition = findMatchingPath(path, statKeys);
			if (matchingConsolidationDefinition != null) {
				TreeSet<Map<String, Object>> dataSet = new TreeSet<Map<String, Object>>(
						new NumComparator());
				for (Map<String, Object> dataMap : serviceData) {
					Map<String, Object> dataVal = new HashMap<String, Object>(getDataVal(dataMap, path));
					if (dataVal != null && !dataVal.isEmpty()) {
						dataVal.put(STAT_TIMESTAMP, dataMap.get(STAT_TIMESTAMP));
						dataSet.add(dataVal);
					}
				}
				putOnPath(path, consolidatedMap, dataSet);
			} else {
				if (templateData.get(key) instanceof Map) {
					consolidateMapsHelper(
							(Map<String, Object>) templateData.get(key),
							consolidatedMap, path, statKeys, serviceData);
				}
			}
			path.removeLast();
		}
	}

	static class NumComparator implements Comparator<Map<String, Object>> {
		protected Object getDataVal(Map<String, Object> dataMap,
				List<String> path) {
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

		//Invariant: date ranges will never be the same
		@Override
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			List<String> path = new LinkedList<String>();
			path.add(STAT_TIMESTAMP);
			Map<String, Object> dateRange1 = (Map<String,Object>)getDataVal(o1, path);
			Map<String, Object> dateRange2 = (Map<String,Object>)getDataVal(o1, path);
			
			
			if (getDataVal(o1, path) != null && getDataVal(o2, path) != null) {
				// if both maps have a num field, order them numerically
				final Long min1 = ((Date) dateRange1.get(GRAIN_MIN)).getTime();
				final Long min2 = ((Date) dateRange2.get(GRAIN_MIN)).getTime();

				// tricky way of returning negative if i1<i2, positive if i1>i2,
				// and 0 if the same
				if (min1 < min2) {
					return -1;
				} else if (min1 > min2) {
					return 1;
				} else {					
					Long max1 = ((Date) dateRange1.get(GRAIN_MAX)).getTime();
					Long max2 = ((Date) dateRange2.get(GRAIN_MAX)).getTime();
					if(max1 < max2){
						return -1;
					} else if(max1 > max2){
						return 1;
					} else {//should never happen
						LOGGER.warn("numComparator attempting to store objects with same timestamp");
						return 0;
					}
				}
			} else if (getDataVal(o1, path) != null) {
				// if only the first map has a num field, put it first
				return -1;
			} else if (getDataVal(o2, path) != null) {
				// if only the second map has a num field, put it first
				return 1;
			} else {
				// if neither map has a num field, put second after first
				return 1;
			}
		}
	}

	// these functions are from the abstract gatherer function just
	// repurposed to do the consolidation to an array instead of combining 
	// the values & reinserting.
	protected Map<String, Object> getDataVal(Map<String, Object> dataMap,
			List<String> path) {
		Map<String, Object> map = dataMap;
		for (String key : path) {
			if (map.containsKey(key)) {
				if (map.get(key) instanceof Map) {
					map = (Map<String, Object>) map.get(key);
				} else {
					return null;
				}
			}
		}
		return map;
	}

	protected void putOnPath(List<String> path, Map<String, Object> statsData,
			Object dataToAdd) {
		Map<String, Object> cursor = statsData;
		int index = 0;
		for (String key : path) {
			if (index == path.size() - 1) {
				cursor.put(key, dataToAdd);
			} else if (cursor.containsKey(key)) {
				if (cursor.get(key) instanceof Map) {
					cursor = (Map<String, Object>) cursor.get(key);
				} else {
					//TODO create path that does not exist
					LOGGER.error("Cannot put data on path");
				}
			}
			index++;
		}
	}

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

			while (pathIndex < path.size()) {// run through it to compare to path
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

	// Above is copied from abstract/embedded grainularity classes

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LOGGER.info("StatsServlet doGet started");
		try {
			try {
				Date upperTimestamp = new Date();
				Date lowerTimestamp = new Date(0L);
				boolean noDate = true;
				// 1. Get a DateRange for ts.min & ts.max
				if (req.getParameter(tsMax) != null) {
					noDate = false;
					if(!req.getParameter(tsMax).equals("")){
						try{
							upperTimestamp = NUMERIC_SEARCH_FORMAT.parse(req
								.getParameter(tsMax));
						} catch(ParseException e) {
							upperTimestamp = new Date();
							LOGGER.error("Error Parsing Date: " + req.getParameter(tsMax), e);
						}
					} else {
						LOGGER.warn("Warning: Max date is not defined");
					}
				}
				if (req.getParameter(tsMin) != null) {
					noDate = false;
					if(!req.getParameter(tsMin).equals("")){
						try{
							lowerTimestamp = NUMERIC_SEARCH_FORMAT.parse(req
								.getParameter(tsMin));
						} catch(ParseException e) {
							lowerTimestamp = new Date(0L);
							LOGGER.error("Error Parsing Date: " + req.getParameter(tsMin), e);
						}
					} else {
						LOGGER.warn("Warning: Min date is not defined");
					}
				} 

				// 2. get all service names
				String[] serviceNames = req
						.getParameterValues(STAT_GATHERER_NAME);
				Integer grainType = null;
				
				if (req.getParameter(GRAIN_TYPE) != null) {//optional
					grainType = Integer.parseInt(req.getParameter(GRAIN_TYPE));
				}
				List<StatsQuery> queryList = null;
				List<StatsFilter> filterList = null;
				
				if (serviceNames == null) {
					LOGGER.error("no valid data input");
					throw new NullPointerException();
				}
				
				if (serviceNames != null) {
					queryList = new LinkedList<StatsQuery>();
					filterList = new LinkedList<StatsFilter>();
					for (String serviceName : serviceNames) {
					//	building query
						StatsQuery query = new StatsQuery(serviceName, grainType, lowerTimestamp, upperTimestamp);
						StatsFilter filter = null;
						String[] keys = req.getParameterValues(serviceName);
					//	building filter
						if (keys != null) {
							Map<String, Object> temp = new HashMap<String, Object>();
							// 4. while looping build a second map for query
							// second map will only have serviceName and date range
							for (String key : keys) {
								temp.put(key, 1);
							}
							filter = new StatsFilter(serviceName, temp);
						} else {
							filter = new StatsFilter(serviceName, null);
						}
						queryList.add(query);
						if(filter.keys != null){
							filter.keys.put("_id", 1);
						}
						filterList.add(filter);
					}
				}	

				List<Map<String, Object>> result = null;

				if (noDate) {//no date range means get instant data
					Map<String, String[]> gatheringStats = new HashMap<String, String[]>();
					for (StatsFilter tempFilter : filterList) {
						if (tempFilter.keys != null) {
							String keys[] = new String[tempFilter.keys
									.keySet().toArray().length];
							int i = 0;
							for (Object obj : tempFilter.keys.keySet()) {
								if (obj instanceof String) {
									keys[i] = (String) obj;
								} else {
									keys[i] = obj.toString();
								}
								i++;
							}
							gatheringStats.put(tempFilter.gathererName, keys);
						} else {
							gatheringStats.put(tempFilter.gathererName, null);
						}
					}
					List<RawStats> tempRawStats = client.gatherData(gatheringStats);
					result = new LinkedList<Map<String,Object>>();
					for(RawStats stats: tempRawStats){
						Map<String,Object> statsMap = stats.toMap();
						result.add(statsMap);
					}
				} else {//output query data as formatted json
					List<Map<String, Object>> stats = client.queryStatistics(
							queryList, filterList);
					result = new LinkedList<Map<String, Object>>();
					for (String name : serviceNames) {	
						List<Map<String, Object>> serviceStats = new LinkedList<Map<String, Object>>();
						//populate service data
						for (Map<String, Object> data : stats) {
							if (data.get(STAT_GATHERER_NAME).equals(name)) {
								serviceStats.add(data);
							}
						}
						Map<String, Object> consolidatedMap = new HashMap<String, Object>();
						consolidatedMap.put(
								name,
								consolidateMaps(serviceStats, client
										.getAttributes(name).getKeys())); 
						result.add(consolidatedMap);
					}
				}

				try {
					// 6. return data from query
					JSONObject statistics = new JSONObject();
					statistics.put("results", new JSONArray(result.toArray()));
					resp.getWriter().write(statistics.toString());
				} catch (IOException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				} catch (JSONException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, resp);
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				}
			} catch (CannotRetrieveException e) {
				respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
				if (LOGGER.isErrorEnabled())
					LOGGER.error("CannotRetrieveException", e);
			} catch (NullPointerException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("NullException thrown", e);
			}
		} catch (IOException e) {
			LOGGER.error("IOException thrown", e);
		}
		LOGGER.info("StatsServlet doGet finished");
	}
}
