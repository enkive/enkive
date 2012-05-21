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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import static com.linuxbox.enkive.search.Constants.*;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.retriever.mongodb.MongoRetrieverService;
import com.linuxbox.enkive.statistics.storage.mongodb.MongoStatsStorageService;

public class StatsServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7062366416188559812L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	/*
		final MongoRetrieverService retriever = new MongoRetrieverService();
		JSONObject statistics = new JSONObject();
		try {
			Date upperDate = null;
			Date lowerDate = null;
			HashMap<String, String[]> hMap = new HashMap<String, String[]>();
			String[] serviceNames = null;
			if (req.getParameter("upperDate") != null) {
				upperDate = NUMERIC_SEARCH_FORMAT.parse(req
						.getParameter("upperDate"));
			}
			if (req.getParameter("lowerDate") != null) {
				lowerDate = NUMERIC_SEARCH_FORMAT.parse(req
						.getParameter("lowerDate"));
			}
			if (req.getParameterValues("serviceNames") != null) {
				serviceNames = req.getParameterValues("serviceNames");
			}

			for (String serviceName : serviceNames) {
				hMap.put(serviceName, req.getParameterValues(serviceName));
			}

			statistics = retriever.retrieveStats(hMap, lowerDate.getTime(),
					upperDate.getTime());

			try {
				// JSONObject jObject = new JSONObject();
				// jObject.put(WebConstants.DATA_TAG, statistics);
				resp.getWriter().write(statistics.toString());
			} catch (IOException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				throw new CannotRetrieveException(
						"could not create JSON for message attachment", e);
			}
			
			 * catch (JSONException e) {
			 * respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
			 * resp); throw new CannotRetrieveException(
			 * "could not create JSON for message attachment", e);}
			 
		} catch (ParseException e) {
			System.out.println("Parse exception");
			System.exit(0);
		} catch (CannotRetrieveException e) {
			respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Could not retrieve stats");
		} catch (JSONException e) {
			System.out.println("JSONException e");
			System.exit(0);
		}
	*/
	}
}