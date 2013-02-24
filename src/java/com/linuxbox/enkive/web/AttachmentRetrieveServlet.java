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
 *******************************************************************************/
package com.linuxbox.enkive.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.ContentException;
import com.linuxbox.enkive.message.EncodedContentReadData;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class AttachmentRetrieveServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7489338160172966335L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String attachmentUUID = req.getParameter("attachmentid");
		final MessageRetrieverService retriever = getMessageRetrieverService();

		try {
			EncodedContentReadData attachment = retriever
					.retrieveAttachment(attachmentUUID);

			// TODO : This file name does not appear to be what we want.
			String filename = attachment.getFilename();
			if (filename == null || filename.isEmpty()) {
				filename = "Message Body";
			}

			try {
				resp.setContentType(attachment.getMimeType());
				resp.setCharacterEncoding("utf-8");
				resp.setHeader("Content-disposition", "attachment;  filename="
						+ filename);

				final InputStream in = attachment.getBinaryContent();
				final OutputStream out = resp.getOutputStream();

				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			} catch (ContentException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("error transferring attachment  "
							+ attachmentUUID, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"error transferring attachment " + attachmentUUID
								+ "; see server logs");
			}
		} catch (CannotRetrieveException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("error retrieving attachment " + attachmentUUID, e);
			}
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error retrieving attachment " + attachmentUUID
							+ "; see server logs");
		}
	}
}
