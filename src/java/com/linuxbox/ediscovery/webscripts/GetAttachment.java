/*
 *  Copyright 2011 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.ediscovery.webscripts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptProcessor;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.AuthenticatingConnector;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

import com.linuxbox.ediscovery.connector.EnkiveAuthenticator;

public class GetAttachment extends AbstractWebScript {

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
		
		Response ticket = connector.get("/enkive/ticket");

		String ticketText = "";
		if (ticket.getStatus().getCode() == 200)
			ticketText = "?alf_ticket=" + ticket.getText();

		URL url = new URL(connector.getEndpoint() + "/enkive/attachment/"
				+ req.getParameterValues("attachmentid")[0] + ticketText);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(true);

		res.setStatus(con.getResponseCode());
		for (String key : con.getHeaderFields().keySet()) {
			res.setHeader(key, con.getHeaderField(key));
		}
		BufferedInputStream attachmentStream = new BufferedInputStream(
				con.getInputStream());
		BufferedOutputStream resOutputStream = new BufferedOutputStream(
				res.getOutputStream());
		int read;
		while ((read = attachmentStream.read()) != -1)
			resOutputStream.write(read);

		resOutputStream.flush();
		resOutputStream.close();
		attachmentStream.close();
		con.disconnect();

	}

}
