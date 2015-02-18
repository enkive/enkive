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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.AttachmentSummary;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class MessageAttachmentDetailServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7489338160172966335L;
	protected static final String KEY_UUID = "UUID";
	protected static final String KEY_FILE_NAME = "filename";
	protected static final String KEY_MIME_TYPE = "mimeType";
	protected static final String PARAM_MSG_ID = "message_id";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		final String messageId = req.getParameter(PARAM_MSG_ID);
		final MessageRetrieverService retriever = getMessageRetrieverService();

		try {
			final Message message = retriever.retrieve(messageId);

			JSONArray attachments = new JSONArray();

			for (AttachmentSummary attachment : message.getContentHeader()
					.getAttachmentSummaries()) {
				JSONObject attachmentObject = new JSONObject();

				String filename = attachment.getFileName();
				if (filename == null || filename.isEmpty()) {
					final String positionString = attachment
							.getPositionString();

					// TODO: revisit this logic; best to assume first attachment
					// is body?
					if (positionString.isEmpty() || positionString.equals("1")) {
						filename = "Message-Body";
					} else {
						filename = "attachment-" + positionString;
					}
				}

				String mimeType = attachment.getMimeType();
				if (mimeType == null) {
					mimeType = "";
				}

				attachmentObject.put(KEY_UUID, attachment.getUuid());
				attachmentObject.put(KEY_FILE_NAME, filename);
				attachmentObject.put(KEY_MIME_TYPE, mimeType);
				attachments.put(attachmentObject);
			}

			JSONObject jObject = new JSONObject();
			jObject.put(WebConstants.DATA_TAG, attachments);
			String jsonString = jObject.toString();
			resp.getWriter().write(jsonString);
		} catch (CannotRetrieveException e) {
			respondError(HttpServletResponse.SC_UNAUTHORIZED, null, resp);
			LOGGER.error("Could not retrieve attachment", e);
		} catch (JSONException e) {
			respondError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null,
					resp);
			LOGGER.error("Could not retrieve attachment", e);
		}
	}
}
