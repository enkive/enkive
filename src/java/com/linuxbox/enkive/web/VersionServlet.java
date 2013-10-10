/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
 *******************************************************************************/
package com.linuxbox.enkive.web;

import static com.linuxbox.enkive.web.WebConstants.VERSION_LOCAL;
import static com.linuxbox.enkive.web.WebConstants.VERSION_REMOTE;
import static com.linuxbox.enkive.web.WebConstants.VERSION_UPGRADE;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.ProductInfo;
import com.linuxbox.enkive.administration.AdministrationService;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.util.Version;

/**
 * This webscript is run when a user wants to see the current local and upstream
 * versions of Enkive
 */
public class VersionServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1226107681645083623L;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.version");

	protected AdministrationService adminService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.adminService = getAdministrationService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		JSONObject jsonResult = new JSONObject();
		Version version = adminService.getVersion();
		try {
			jsonResult.put(VERSION_LOCAL, ProductInfo.VERSION.toString());
			if (version != null) {
				jsonResult.put(VERSION_REMOTE, version.toString());
				jsonResult.put(VERSION_UPGRADE, !version.equals(ProductInfo.VERSION));
			} else {
				jsonResult.put(VERSION_REMOTE, "Unknown");
				jsonResult.put(VERSION_UPGRADE, false);
			}

			res.getWriter().write(jsonResult.toString());
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Unable to serialize JSON", e);
		}
	}
}
