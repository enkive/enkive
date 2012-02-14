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

import static com.linuxbox.enkive.web.WebConstants.ERRORS_TAG;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8772085078547445222L;
	protected static final Log logger = LogFactory
			.getLog("com.linuxbox.enkive.webscripts");

	public AbstractJsonServlet() {
		super();
	}

	protected void addError(JSONObject queryResult, String errorMessage) {
		try {
			JSONArray errors = queryResult.optJSONArray(ERRORS_TAG);
			if (errors == null) {
				errors = new JSONArray();
				queryResult.put(ERRORS_TAG, errors);
			}
			errors.put(errorMessage);
		} catch (JSONException e) {
			logger.error("could not add error to JSON query result");
		}
	}

	protected void setResponse(HttpServletResponse response,
			JSONObject jsonObject) {
		String jsonResultString = jsonObject.toString();
		try {
			response.getWriter().write(jsonResultString);
		} catch (IOException e) {
			logger.error("Could not write webscript response");
		}
	}
}
