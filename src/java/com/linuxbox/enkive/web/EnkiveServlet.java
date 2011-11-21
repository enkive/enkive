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

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.permissions.PermissionService;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.workspace.WorkspaceService;
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

	public MessageRetrieverService getMessageRetrieverService() {
		return appContext.getBean(MessageRetrieverService.class);
	}

	public AuditService getAuditService() {
		return appContext.getBean("AuditLogService", AuditService.class);
	}
	
	public AuthenticationService getAuthenticationService() {
		return appContext.getBean(AuthenticationService.class);
	}
	
	public WorkspaceService getWorkspaceService() {
		return appContext.getBean(WorkspaceService.class);
	}
	
	public MessageSearchService getMessageSearchService() {
		return appContext.getBean(MessageSearchService.class);
	}
	
	public PermissionService getPermissionService() {
		return appContext.getBean(PermissionService.class);
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
