/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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

import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.MimeUtility;
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
	private static final String PARAM_ATTACHMENT_ID = "attachmentid";
	private static final String PARAM_FILE_NAME = "fname";
	private static final String PARAM_MIME_TYPE = "mtype";
	private static final String DEFAULT_FILE_NAME = "Message Body";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		final MessageRetrieverService retriever = getMessageRetrieverService();
		final String attachmentUUID = req.getParameter(PARAM_ATTACHMENT_ID);

		try {
			EncodedContentReadData attachment = retriever
					.retrieveAttachment(attachmentUUID);

			String filename = req.getParameter(PARAM_FILE_NAME);
			if (filename == null || filename.isEmpty()) {
				filename = attachment.getFilename();
			}
			if (filename == null || filename.isEmpty()) {
				filename = DEFAULT_FILE_NAME;
			}

			String mimeType = req.getParameter(PARAM_MIME_TYPE);
			if (mimeType == null || mimeType.isEmpty()) {
				mimeType = attachment.getMimeType();
			}

			// is there a purpose to the nested trys?
			try {
				if (mimeType != null) {
					resp.setContentType(mimeType);
				}
				resp.setCharacterEncoding("utf-8");
				resp.setHeader("Content-disposition", "attachment;  filename="
						+ MimeUtility.quote(filename, HeaderTokenizer.MIME));

				final InputStream in = attachment.getBinaryContent();
				final OutputStream out = resp.getOutputStream();

				IOUtils.copy(in, out);
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			} catch (ContentException e) {
				LOGGER.error(
						"error transferring attachment  " + attachmentUUID, e);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"error transferring attachment " + attachmentUUID
								+ "; see server logs");
			}
		} catch (CannotRetrieveException e) {
			LOGGER.error("error retrieving attachment " + attachmentUUID, e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error retrieving attachment " + attachmentUUID
							+ "; see server logs");
		}
	}
}
