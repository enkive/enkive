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

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import static com.linuxbox.enkive.search.Constants.*;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class StatsServlet extends EnkiveServlet {
	final StatsClient retriever = getStatsClient();

	private static final long serialVersionUID = 7062366416188559812L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			try {
				Date upperDate = null;
				Date lowerDate = null;
				if (req.getParameter("upperDate") != null) {
					upperDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter("upperDate"));
				}
				if (req.getParameter("lowerDate") != null) {
					lowerDate = NUMERIC_SEARCH_FORMAT.parse(req
							.getParameter("lowerDate"));
				}

				String[] serviceNames = req.getParameterValues("serviceNames");
				Map<String, String[]> map = new HashMap<String, String[]>();

				resp.getWriter().write("serviceNames: " + serviceNames + "\n");

				if (serviceNames != null) {
					for (String serviceName : serviceNames) {
						resp.getWriter().write(
								"serviceName: " + serviceName + "\n");
						map.put(serviceName,
								req.getParameterValues(serviceName));
					}
				}

				resp.getWriter().write("map: " + map + "\n");
				// resp.getWriter().write("lowerDate: " + lowerDate + "\n");
				// resp.getWriter().write("upperDate: " + upperDate + "\n");

				Set<Map<String, Object>> stats = retriever.queryStatistics(map,
						lowerDate, upperDate);

				// resp.getWriter().write("stats: " + stats + "\n");

				try {
					JSONArray statistics = new JSONArray(stats.toArray());
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
			} catch (ParseException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("Error Parsing Data", e);
			} catch (CannotRetrieveException e) {
				respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Could not retrieve stats");
			} catch (NullPointerException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				LOGGER.error("NullException thrown", e);
			}
		} catch (IOException e) {
			LOGGER.error("IOException thrown", e);
		}
	}
}