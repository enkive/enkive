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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class MessageRetrieveServlet extends EnkiveServlet {

	private static final long serialVersionUID = -2193199657369345679L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doGet(req, resp);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		final String messageId = req.getParameter("message_id");
		final MessageRetrieverService retriever = getMessageRetrieverService();

		try {
			final Message message = retriever.retrieve(messageId);
			if (LOGGER.isTraceEnabled())
				LOGGER.trace("retrieving message " + messageId);

			final ServletOutputStream outputStream = resp.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					outputStream));
			message.pushReconstitutedEmail(writer);
			resp.flushBuffer();
		} catch (CannotRetrieveException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("error retrieving message " + messageId, e);
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error retrieving message " + messageId
							+ "; see server logs", resp);
		}
	}
}
