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
package com.linuxbox.ediscovery.webscripts;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;

public class ExportMbox extends AbstractWebScript {

	public static String MBOX_RETRIEVE_REST_URL = "/search/export/mbox?searchid=";
	public static String EDISCOVERY_RECENT_SEARCH_URL = "/ediscovery/search/recent/view?searchid=";

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res)
			throws IOException {

		ScriptDetails script = getExecuteScript(req.getContentType());
		Map<String, Object> model = new HashMap<String, Object>();
		Map<String, Object> scriptModel = createScriptParameters(req, res,
				script, model);
		Map<String, Object> returnModel = new HashMap<String, Object>();
		scriptModel.put("model", returnModel);
		executeScript(script.getContent(), scriptModel);

		ScriptProcessor scriptProcessor = getContainer()
				.getScriptProcessorRegistry().getScriptProcessor(
						script.getContent());

		ScriptRemote remote = (ScriptRemote) scriptProcessor
				.unwrapValue(scriptModel.get("remote"));

		ScriptRemoteConnector connector = remote.connect("enkive");

		Response resp = connector.call(MBOX_RETRIEVE_REST_URL
				+ req.getParameterValues("searchid")[0]);

		if (resp.getStatus().getCode() == ResponseStatus.STATUS_FORBIDDEN) {
			System.out.println("HERE");
			res.setContentType("text/plain");
			res.setStatus(ResponseStatus.STATUS_MOVED_TEMPORARILY);
			res.setHeader(
					"Location",
					EDISCOVERY_RECENT_SEARCH_URL
							+ req.getParameterValues("searchid")[0]);
			// BufferedWriter resWriter = new BufferedWriter(res.getWriter());
			// resWriter.write("You must be logged in to download mbox exports");
			// resWriter.flush();
			// resWriter.close();
		} else {
			res.setContentType("text/plain");
			res.setHeader("Content-disposition",
					"attachment; filename=export.mbox");
			res.setStatus(resp.getStatus().getCode());
			for (String key : resp.getStatus().getHeaders().keySet()) {
				res.setHeader(key, resp.getStatus().getHeaders().get(key));
			}

			BufferedInputStream mboxStream = new BufferedInputStream(
					resp.getResponseStream());
			BufferedWriter resWriter = new BufferedWriter(res.getWriter());
			int read;
			while ((read = mboxStream.read()) != -1)
				resWriter.write(read);

			resWriter.flush();
			resWriter.close();
			mboxStream.close();
		}
	}
}
