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
package com.linuxbox.enkive.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import com.linuxbox.enkive.statistics.gathering.GathererAttributes;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class KeyNameServlet extends EnkiveServlet {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web.StatsServlet");
	private static final long serialVersionUID = 7062366416188559812L;

	private StatsClient client;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		client = getStatsClient();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		LOGGER.info("KeyNameServlet doGet started");
		try {
			try {
				Set<GathererAttributes> attributes = client.getAttributes();
				JSONArray result = new JSONArray();

				for (GathererAttributes attribute : attributes) {
					if (!attribute.getName().equals("CollectionStatsService")) {
						Map<String, Object> keyMethods = new HashMap<String, Object>();
						for (ConsolidationKeyHandler key : attribute.getKeys()) {
							String builtKey = "";
							for (String keyPart : key.getKey()) {
								builtKey = builtKey + keyPart + ".";
							}
							builtKey = builtKey.substring(0,
									builtKey.length() - 1);
							if (key.getMethods() != null) {
								Map<String, Object> methodsMap = new HashMap<String, Object>();
								Map<String, Object> innerKeyMap = new HashMap<String, Object>();
								innerKeyMap.put("methods", (String[]) key
										.getMethods().toArray());
								innerKeyMap.put("units", key.getUnits());
								methodsMap.put(builtKey, innerKeyMap);
								keyMethods.put(key.getHumanKey(), methodsMap);
							}
						}
						Map<String, Object> dataTemp = new HashMap<String, Object>();
						dataTemp.put(attribute.getName(), keyMethods);
						Map<String, Object> humanKeyWrapper = new HashMap<String, Object>();
						humanKeyWrapper.put(attribute.getHumanName(), dataTemp);
						result.put(humanKeyWrapper);
					}
				}

				try {
					JSONObject statistics = new JSONObject();
					statistics.put("results", result);
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
