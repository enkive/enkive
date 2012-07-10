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
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MAX;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_MIN;
import static com.linuxbox.enkive.statistics.granularity.GrainConstants.GRAIN_TYPE;

import java.io.IOException;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.statistics.KeyConsolidationHandler;
import com.linuxbox.enkive.statistics.services.StatsClient;

@SuppressWarnings("unchecked")
public class StatsServlet extends EnkiveServlet {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.StatsServlet");
	private static final long serialVersionUID = 7062366416188559812L;

	private StatsClient retriever;

	/*
	 * Servlet Algorithm 1. Get a DateRange for ts.min & ts.max (done) 1.5 if no
	 * dateRange is specified figure out a way to do instantData 2. get all
	 * service names 3. loop over service names to build filter map 4. while
	 * looping build a second map for query second map will only have
	 * serviceName, date range, and (optional) grainType 5. query database using
	 * query map & filter map 6. format query data 7. return formatted data
	 * Note. handle errors with log messages & thrown exceptions
	 */

	private final String tsMax = STAT_TIME_STAMP + "." + GRAIN_MAX;
	private final String tsMin = STAT_TIME_STAMP + "." + GRAIN_MIN;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		retriever = getStatsClient();
	}

	/*
	 * JSON formatting algorithm 1. get a map to use as template 2. recurse
	 * template until hit full path specified by attributes class 3. when you
	 * have full path loop over entire data on that path to build a sorted set
	 * (sorted on ts.min) for each input to the set also include ts and _id 4.
	 * when done with that loop store that set under the path on the
	 * consolidated data path 5. return Set<Map<String,Object>> as json by list
	 * constructor
	 */

	private Map<String, Object> consolidateMaps(
			Set<Map<String, Object>> serviceData, List<KeyConsolidationHandler> statKeys) {
		if (serviceData.size() > 0) {
			Map<String, Object> template = new HashMap<String, Object>(
					serviceData.iterator().next());
			template.remove(STAT_SERVICE_NAME);
			template.remove(STAT_TIME_STAMP);
			template.remove("_id");
			LinkedList<String> path = new LinkedList<String>();
			Map<String, Object> result = new HashMap<String, Object>(template);
			consolidateMapsHelper(template, result, path, statKeys, serviceData);
			return result;
		}
		return null;
	}

	private void consolidateMapsHelper(Map<String, Object> templateData,
			Map<String, Object> consolidatedMap, LinkedList<String> path,
			List<KeyConsolidationHandler> statKeys, Set<Map<String, Object>> serviceData) {
		for (String key : templateData.keySet()) {
			path.addLast(key);
			KeyConsolidationHandler matchingConsolidationDefinition = findMatchingPath(path, statKeys);
			if (matchingConsolidationDefinition != null) {
				TreeSet<Map<String, Object>> dataSet = new TreeSet<Map<String, Object>>(
						new NumComparator());
				for (Map<String, Object> dataMap : serviceData) {
					Map<String, Object> dataVal = getDataVal(dataMap, path);
					dataVal.put(STAT_TIME_STAMP, dataMap.get(STAT_TIME_STAMP));
					dataVal.put("_id", dataMap.get("_id"));
					if (dataVal != null) {
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

		@Override
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			List<String> path = new LinkedList<String>();
			path.add(STAT_TIME_STAMP);
			path.add(GRAIN_MIN);
			if (getDataVal(o1, path) != null && getDataVal(o2, path) != null) {
				// if both maps have a num field, order them numerically
				final Long l1 = (Long) getDataVal(o1, path);
				final Long l2 = (Long) getDataVal(o2, path);

				// tricky way of returning negative if i1<i2, positive if i1>i2,
				// and 0 if the same
				if (l1 < l2) {
					return -1;
				} else if (l1 > l2) {
					return 1;
				} else {
					return 0;
				}
			} else if (getDataVal(o1, path) != null) {
				// if only the first map has a num field, put it first
				return -1;
			} else if (getDataVal(o2, path) != null) {
				// if only the second map has a num field, put it first
				return 1;
			} else {
				// if neither map has a num field, don't care
				return 0;
			}
		}
	}

	// TODO: these functions are from the abstract gatherer function just
	// repurposed to do
	// the consolidation to an array instead of combining the values
	protected Map<String, Object> getDataVal(Map<String, Object> dataMap,
			List<String> path) {
		Map<String, Object> map = dataMap;
		for (String key : path) {
			if (((Map<String, Object>) map).containsKey(key)) {
				if (((Map<String, Object>) map).get(key) instanceof Map) {
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
					LOGGER.error("Cannot put data on path");
				}
			}
			index++;
		}
	}

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
			}// no matches found
		}
		return null;
	}

	// TODO Above is copied from abstract/embedded grainularity classes

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LOGGER.info("StatsServlet doGet started");
		try {
			try {
				Date upperDate = new Date();
				Date lowerDate = new Date(0L);
				boolean noDate = true;
				// 1. Get a DateRange for ts.min & ts.max
				if (req.getParameter(tsMax) != null) {
					noDate = false;
					upperDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter(tsMax));
				}
				if (req.getParameter(tsMin) != null) {
					noDate = false;
					lowerDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter(tsMin));
				}

				// 2. get all service names
				String[] serviceNames = req
						.getParameterValues(STAT_SERVICE_NAME);
				Map<String, Map<String, Object>> query = new HashMap<String, Map<String, Object>>();
				Map<String, Map<String, Object>> filter = new HashMap<String, Map<String, Object>>();
				Map<String, Object> grainMap = null;
				Map<String, Object> dateMap = null;

				if (req.getParameter(GRAIN_TYPE) != null) {// optional grain
															// type field
					grainMap = new HashMap<String, Object>();
					grainMap.put(GRAIN_TYPE,
							Integer.parseInt(req.getParameter(GRAIN_TYPE)));
				}
				if (!noDate) {
					dateMap = new HashMap<String, Object>();
					Map<String, Object> upperMap = new HashMap<String, Object>();
					upperMap.put("$lt", upperDate.getTime());
					Map<String, Object> lowerMap = new HashMap<String, Object>();
					lowerMap.put("$gte", lowerDate.getTime());
					dateMap.put(tsMax, upperMap);
					dateMap.put(tsMin, lowerMap);
				}

				// TODO resp.getWriter().write("serviceNames: " +
				// Arrays.toString(serviceNames) + "\n");

				if (serviceNames == null && noDate) {
					LOGGER.error("no valid data input");
					throw new NullPointerException();
				}

				// 3. loop over service names to build filter map
				if (serviceNames != null) {
					for (String serviceName : serviceNames) {
						// TODO resp.getWriter().write("serviceName: " +
						// serviceName + "\n");
						// building query
						Map<String, Object> tempQuery = new HashMap<String, Object>();
						if (grainMap != null) {
							tempQuery.putAll(grainMap);

						}
						if (dateMap != null) {
							tempQuery.putAll(dateMap);
						}
						query.put(serviceName, tempQuery);

						String[] keys = req.getParameterValues(serviceName);
						// TODO resp.getWriter().write("keys: " +
						// Arrays.toString(keys) + "\n");
						// building filter
						if (keys != null) {
							Map<String, Object> temp = new HashMap<String, Object>();
							temp.put(STAT_SERVICE_NAME, 1);
							if (!noDate) {
								temp.put(tsMax, 1);
								temp.put(tsMin, 1);
							} else {
								temp.put(STAT_TIME_STAMP, 1);
							}
							// 4. while looping build a second map for query
							// second map will only have serviceName and date
							// range
							for (String key : keys) {
								temp.put(key, 1);
							}
							filter.put(serviceName, temp);
						} else {
							filter.put(serviceName, null);
						}
					}
				}
				// TODO resp.getWriter().write("query: " + query + "\n");
				// TODO resp.getWriter().write("filter: " + filter + "\n");

				// requires at least one serviceName
				if (query.isEmpty()) {
					throw new NullPointerException();
				}

				Set<Map<String, Object>> result = null;

				if (noDate) {// 1.5 if no dateRange is specified figure out a
								// way to do instantData
					Map<String, String[]> gatheringStats = new HashMap<String, String[]>();
					// TODO resp.getWriter().write("returning raw data" + "\n");
					for (String name : filter.keySet()) {
						// TODO resp.getWriter().write("name: " + name);
						if (filter.get(name) != null) {
							String keys[] = new String[filter.get(name)
									.keySet().toArray().length];
							int i = 0;
							for (Object obj : filter.get(name).keySet()
									.toArray()) {
								if (obj instanceof String) {
									keys[i] = (String) obj;
								} else {
									keys[i] = obj.toString();
								}
								i++;
							}
							// TODO resp.getWriter().write("-- keys: " +
							// Arrays.toString(keys) + "\n");
							gatheringStats.put(name, keys);
						} else {
							gatheringStats.put(name, null);
						}
					}
					result = retriever.gatherData(gatheringStats);
				} else {// 5. query database using query map & filter map
					Set<Map<String, Object>> stats = retriever.queryStatistics(
							query, filter);
					result = new HashSet<Map<String, Object>>();
					for (String name : serviceNames) {
						// TODO resp.getWriter().write("name: " + name + "\n");
						Set<Map<String, Object>> serviceStats = new HashSet<Map<String, Object>>();
						for (Map<String, Object> data : stats) {
							if (data.get(STAT_SERVICE_NAME).equals(name)) {
								serviceStats.add(data);
							}
						}
						// TODO resp.getWriter().write("serviceStats: " +
						// serviceStats + "\n");
						Map<String, Object> consolidatedMap = new HashMap<String, Object>();
						consolidatedMap.put(
								name,
								consolidateMaps(serviceStats, retriever
										.getAttributes(name).getKeys()));
						// TODO resp.getWriter().write("consolidatedMap: " +
						// consolidatedMap + "\n");
						result.add(consolidatedMap);
					}
				}
				// TODO resp.getWriter().write("result: " + result + "\n");

				try {
					// 6. return data from query
					JSONArray statistics = new JSONArray(result.toArray());
					resp.getWriter().write("JSON: " + statistics.toString());
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
			} catch (ParseException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("Error Parsing Data", e);
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
