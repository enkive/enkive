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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;
import org.springframework.util.FileCopyUtils;

public class ExportSearchFolder extends AbstractWebScript {

	public static String SEARCHFOLDER_EXPORT_REST_URL = "/search/searchFolder?action=export";
	public static String EDISCOVERY_SEARCHFOLDER_URL = "/ediscovery/search/folder";

	protected ScriptRemote scriptRemote;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {

		ScriptRemoteConnector connector = scriptRemote.connect("enkive");

		Response resp = connector.call(SEARCHFOLDER_EXPORT_REST_URL);

		if (resp.getStatus().getCode() == ResponseStatus.STATUS_FORBIDDEN) {
			res.setContentType("text/plain");
			res.setStatus(ResponseStatus.STATUS_MOVED_TEMPORARILY);
			res.setHeader("Location", EDISCOVERY_SEARCHFOLDER_URL);
		} else {
			res.setHeader("Content-disposition",
					"attachment; filename=SearchFolder.tar.gz");
			res.setContentType("application/x-gzip");
			res.setContentEncoding(resp.getEncoding());
			res.setStatus(resp.getStatus().getCode());
			for (String key : resp.getStatus().getHeaders().keySet()) {
				res.setHeader(key, resp.getStatus().getHeaders().get(key));
			}
			try {
				IOUtils.copy(resp.getResponseStream(), res.getOutputStream());
				//FileCopyUtils.copy(resp.getResponseStream(), res.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public ScriptRemote getScriptRemote() {
		return scriptRemote;
	}

	public void setScriptRemote(ScriptRemote scriptRemote) {
		this.scriptRemote = scriptRemote;
	}
}