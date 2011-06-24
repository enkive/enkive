package com.linuxbox.enkive.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DocumentSearchServlet extends EnkiveServlet {
	private static final long serialVersionUID = 5469321957476311038L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();
		out.println("<html><body>Hello, <b>user</b>. This is TestServlet!</body></html>");
	}
}
