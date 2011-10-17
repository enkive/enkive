package com.linuxbox.enkive.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

import static com.linuxbox.enkive.search.Constants.*;

public class MessageSearchServlet extends EnkiveServlet {
	private static final long serialVersionUID = 5469321957476311038L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doGet(req, resp);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			final MessageSearchService messageSearchService = getMessageSearchService();
			String searchSubject = req.getParameter("search_subject");
			String searchSender = req.getParameter("search_sender");
			String searchRecipient = req.getParameter("search_recipient");
			String searchDateEarliest = req.getParameter("search_dateEarliest");
			String searchDateLatest = req.getParameter("search_dateLatest");
			String searchContent = req.getParameter("search_content");
			
			HashMap<String, String> fields = new HashMap<String, String>();
			fields.put(SUBJECT_PARAMETER, searchSubject);
			fields.put(RECIPIENT_PARAMETER, searchRecipient);
			fields.put(SENDER_PARAMETER, searchSender);
			fields.put(DATE_EARLIEST_PARAMETER, searchDateEarliest);
			fields.put(DATE_LATEST_PARAMETER, searchDateLatest);
			fields.put(CONTENT_PARAMETER, searchContent);
			
			LOGGER.trace("submitted search fields: \"" + fields.toString()
					+ "\"");
			
			Set<String> result = messageSearchService.search(fields);
			
			LOGGER.trace("message search yield: " + result.size() + " messages");
			req.setAttribute("message_id_list", result);
			req.setAttribute("search_fields", fields);
			forward("/messageSearchResults.jsp", req, resp);
		} catch (MessageSearchException e) {
			throw new ServletException(
					"could not search through document index", e);
		}
	}
}
