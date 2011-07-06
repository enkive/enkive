package com.linuxbox.enkive.web;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.util.spring.ApplicationContextProvider;

public class EnkiveServlet extends HttpServlet {
	private static final long serialVersionUID = 7532961482208890586L;

	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.web");

	protected ApplicationContext appContext;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		appContext = ApplicationContextProvider.getApplicationContext();
	}

	public DocSearchQueryService getDocSearchQueryService() {
		return appContext.getBean(DocSearchQueryService.class);
	}

	public DocStoreService getDocStoreService() {
		return appContext.getBean(DocStoreService.class);
	}

	/**
	 * Helper function to make forwarding easier.
	 * 
	 * @param url
	 * @param req
	 * @param resp
	 * @throws ServletException
	 * @throws IOException
	 */
	public void forward(String url, ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		final RequestDispatcher dispatcher = req.getRequestDispatcher(url);
		dispatcher.forward(req, resp);
	}
}
