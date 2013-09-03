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
 ******************************************************************************/
package com.linuxbox.ediscovery.webscripts;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.ScriptRemote;
import org.springframework.extensions.webscripts.ScriptRemoteConnector;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;

/**
 * This class does not currently seem to be exposed in the e-discovery user
 * interface. This presently does not seem to request a specific search folder
 * from Enkive, so it's unclear how it might work.
 * 
 * @author eric
 * 
 */
public class ExportSearchFolder extends AbstractWebScript {
	protected static final String SEARCHFOLDER_EXPORT_REST_URL = "/search/searchFolder?action=export";
	protected static final String EDISCOVERY_SEARCHFOLDER_URL = "/ediscovery/search/folder";
	protected static final String EXPORT_FILE_NAME = "SearchFolder.tar.gz";

	protected ScriptRemote scriptRemote;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {

		ScriptRemoteConnector connector = scriptRemote.connect("enkive");

		Response enkiveResponse = connector.call(SEARCHFOLDER_EXPORT_REST_URL);

		if (enkiveResponse.getStatus().getCode() == ResponseStatus.STATUS_FORBIDDEN) {
			res.setContentType("text/plain");
			res.setStatus(ResponseStatus.STATUS_MOVED_TEMPORARILY);
			res.setHeader("Location", EDISCOVERY_SEARCHFOLDER_URL);
		} else {
			res.setHeader("Content-disposition", "attachment; filename="
					+ EXPORT_FILE_NAME);
			res.setContentType("application/x-gzip");
			res.setContentEncoding(enkiveResponse.getEncoding());
			res.setStatus(enkiveResponse.getStatus().getCode());
			for (String key : enkiveResponse.getStatus().getHeaders().keySet()) {
				res.setHeader(key,
						enkiveResponse.getStatus().getHeaders().get(key));
			}
			try {
				InputStream in = enkiveResponse.getResponseStream();
				OutputStream out = res.getOutputStream();
				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
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