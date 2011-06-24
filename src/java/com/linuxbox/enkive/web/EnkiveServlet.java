package com.linuxbox.enkive.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
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
}
