package com.linuxbox.enkive.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;

public class TestServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7489338160172966335L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
		PrintWriter out = resp.getWriter();
		out.println("<html><body>");
		out.println("Hello, <b>" + currentUser
				+ "</b>. This is TestServlet!");
		if (getDocSearchQueryService() != null) {
			out.println("<p>got doc search query service!</p>");
		} else {
			out.println("<p>did NOT get doc search query service!</p>");

		}
		out.println("</body></html>");
	}
}