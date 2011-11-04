package com.linuxbox.enkive.message.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchQuery;
import com.linuxbox.enkive.workspace.SearchResult;
import com.linuxbox.enkive.workspace.SearchResult.Status;
import com.linuxbox.enkive.workspace.Workspace;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.WorkspaceService;

public abstract class AbstractMessageSearchService implements
		MessageSearchService {

	protected AuthenticationService authenticationService;
	protected WorkspaceService workspaceService;
	protected AuditService auditService;
	protected DocSearchQueryService docSearchService;

	@Override
	public Set<String> search(HashMap<String, String> fields)
			throws MessageSearchException {

		SearchQuery query = new SearchQuery();
		query.setCriteria(fields);

		try {
			Workspace workspace = workspaceService.getActiveWorkspace();
			
			query.setId(workspaceService.saveSearchQuery(query));
			
			SearchResult result = new SearchResult();
			result.setSearchQueryId(query.getId());
			result.setMessageIds(searchImpl(fields));
			result.setTimestamp(new Date());
			result.setExecutedBy(authenticationService.getUserName());
			result.setStatus(Status.COMPLETE);
			workspaceService.saveSearchResult(result);
			
			workspace.addSearchResult(result.getId());
			workspaceService.saveWorkspace(workspace);
			
			return result.getMessageIds();
		} catch (WorkspaceException e) {
			throw new MessageSearchException("Could not save search query", e);
		} catch (AuthenticationException e) {
			throw new MessageSearchException("Could not get authenticated user for search", e);
		}
	}

	@Override
	public Set<String> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException {
		// TODO Auto-generated method stub
		return null;
	}

	protected abstract Set<String> searchImpl(HashMap<String, String> fields)
			throws MessageSearchException;

	public DocSearchQueryService getDocSearchService() {
		return docSearchService;
	}

	public void setDocSearchService(DocSearchQueryService docSearchService) {
		this.docSearchService = docSearchService;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public WorkspaceService getWorkspaceService() {
		return workspaceService;
	}

	public void setWorkspaceService(WorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
