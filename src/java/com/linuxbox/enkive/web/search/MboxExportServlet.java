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
package com.linuxbox.enkive.web.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.EnkiveServletException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.web.EnkiveServlet;
import com.linuxbox.enkive.web.WebScriptUtils;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;

public class MboxExportServlet extends EnkiveServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8545266502891778574L;
	protected WorkspaceService workspaceService;
	protected MessageRetrieverService archiveService;
	protected AuditService auditService;
	protected PermissionService permService;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.webscripts.search.export");

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.workspaceService = getWorkspaceService();
		this.archiveService = getMessageRetrieverService();
		this.auditService = getAuditService();
		this.permService = getPermissionService();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws IOException {
		String searchId = WebScriptUtils.cleanGetParameter(req, "searchid");
		res.setContentType("text/plain");

		res.setCharacterEncoding("UTF-8");
		try {
			SearchQuery search = workspaceService.getSearch(searchId);
			if (search == null) {
				// null if searchId refers to a search query and it had no
				// search results
				throw new EnkiveServletException("search query " + searchId
						+ " had no results");
			}

			Collection<String> messageIds = search.getResult().getMessageIds().values();

			Writer writer = res.getWriter();
			File tempFile = File.createTempFile("enkive-export-", ".mbox");
			String tempName = tempFile.getAbsolutePath();

			BufferedWriter output = new BufferedWriter(new FileWriter(tempFile));
			String tmpLine;
			for (String messageId : messageIds) {
				try {
					Message message = archiveService.retrieve(messageId);

					String mailFrom = message.getMailFrom();
					if (mailFrom.isEmpty()) {
						mailFrom = message.getFromStr();
						if (mailFrom.contains(" ")) {
							mailFrom = mailFrom.substring(0, mailFrom.indexOf(" "));
						}
					}
					if (mailFrom.isEmpty()) {
						mailFrom = "unknown";
					}
					output.write("From " + mailFrom + " " + message.getDateStr() + "\r\n");
					BufferedReader reader = new BufferedReader(
							new StringReader(message.getReconstitutedEmail()));
					while ((tmpLine = reader.readLine()) != null) {
						if (tmpLine.startsWith("From "))
							output.write(">" + tmpLine);
						else
							output.write(tmpLine);
						output.write("\r\n");
					}
				} catch (CannotRetrieveException e) {
					respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							null, res);
					LOGGER.error("Could not retrieve message with id"
							+ messageId);
				}
				output.write("\r\n");
				auditService.addEvent(AuditService.SEARCH_EXPORTED,
						permService.getCurrentUsername(),
						"Search Exported to mbox - ID:" + searchId);
			}
			output.close();
			writer.write(tempName);
		} catch (WorkspaceException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException(
					"unable to access workspace or access search or result with id "
							+ searchId);
		} catch (AuditServiceException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					res);
			throw new EnkiveServletException("Could not write to audit log");
		}
	}
}
