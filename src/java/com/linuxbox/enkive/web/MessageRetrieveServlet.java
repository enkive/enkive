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
			LOGGER.trace("retrieving message " + messageId);

			final ServletOutputStream outputStream = resp.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			message.pushReconstitutedEmail(writer);
			resp.flushBuffer();
		} catch (CannotRetrieveException e) {
			LOGGER.error("error retrieving message " + messageId, e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error retrieving message " + messageId
							+ "; see server logs");
		}
	}
}