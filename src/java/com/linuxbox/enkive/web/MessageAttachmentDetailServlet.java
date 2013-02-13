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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentReadData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class MessageAttachmentDetailServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7489338160172966335L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		final String messageId = req.getParameter("message_id");
		final MessageRetrieverService retriever = getMessageRetrieverService();

		try {
			final Message message = retriever.retrieve(messageId);
			JSONArray attachments = new JSONArray();

			for (String attachmentUUID : message.getContentHeader()
					.getAttachmentUUIDs()) {

				EncodedContentReadData attachment = retriever
						.retrieveAttachment(attachmentUUID);
				JSONObject attachmentObject = new JSONObject();

				String filename = attachment.getFilename();
				if (filename == null || filename.isEmpty()) {
					filename = "Message Body";
				}

				String mimeType = attachment.getMimeType();
				if (mimeType == null) {
					mimeType = "";
				}

				attachmentObject.put("UUID", attachmentUUID);
				attachmentObject.put("filename", filename);
				attachmentObject.put("mimeType", mimeType);
				attachments.put(attachmentObject);
			}
			
			try {
				JSONObject jObject = new JSONObject();
				jObject.put(WebConstants.DATA_TAG, attachments);
				String jsonString = jObject.toString();
				resp.getWriter().write(jsonString);
			} catch (JSONException e) {
				respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						null, resp);
				throw new CannotRetrieveException(
						"could not create JSON for message attachment", e);
			}
		} catch (CannotRetrieveException e) {
			respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Could not retrieve attachment");
			}
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					resp);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Could not retrieve attachment");
			}
		}
	}
}
