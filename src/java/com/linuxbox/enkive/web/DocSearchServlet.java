package com.linuxbox.enkive.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public class DocSearchServlet extends EnkiveServlet {
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
			final DocSearchQueryService queryService = getDocSearchQueryService();
			String searchTerms = req.getParameter("search-terms");
			List<String> result = queryService.search(searchTerms);
			req.setAttribute("doc-id-list", result);
			forward("docSearchResults.jsp", req, resp);
		} catch (DocSearchException e) {
			throw new ServletException(
					"could not search through document index", e);
		}
	}
}
