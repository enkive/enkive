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

public class GetAttachment extends AbstractWebScript {

	public static String ATTACHMENT_RETRIEVE_REST_URL = "/attachment/retrieve?attachmentid=";

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

		Response resp = connector.call(ATTACHMENT_RETRIEVE_REST_URL
				+ req.getParameterValues("attachmentid")[0]);

		if (resp.getStatus().getCode() == ResponseStatus.STATUS_FORBIDDEN) {
			res.setStatus(resp.getStatus().getCode());
			BufferedWriter resWriter = new BufferedWriter(res.getWriter());
			resWriter.write("You must be logged in to download attachments");
			resWriter.flush();
			resWriter.close();

		} else {
			res.setStatus(resp.getStatus().getCode());
			for (String key : resp.getStatus().getHeaders().keySet()) {
				res.setHeader(key, resp.getStatus().getHeaders().get(key));
			}

			BufferedInputStream attachmentStream = new BufferedInputStream(
					resp.getResponseStream());
			BufferedWriter resWriter = new BufferedWriter(res.getWriter());
			int read;
			while ((read = attachmentStream.read()) != -1)
				resWriter.write(read);

			resWriter.flush();
			resWriter.close();
			attachmentStream.close();
		}
	}

}
