package com.linuxbox.enkive.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.EncodedContentData;
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
			EncodedContentData attachment = retriever
					.retrieveAttachment(attachmentUUID);

			String filename = attachment.getFilename();
			if (attachment.getFilename() == null
					|| attachment.getFilename().isEmpty()) {
				filename = "Message Body";
			}
			try {
				filename = "filename=" + filename;
				resp.setContentType(attachment.getMimeType());
				resp.setCharacterEncoding("utf-8");
				resp.setHeader("Content-disposition", "attachment;  "
						+ filename);
				attachment.transferBinaryContent(resp.getOutputStream());
			} catch (CannotTransferMessageContentException e) {
				e.printStackTrace();
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