package com.linuxbox.enkive.message.search;

import java.util.HashMap;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.authentication.AuthenticationException;
import com.linuxbox.enkive.authentication.AuthenticationService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;

public class AuditLoggingMessageSearchService implements MessageSearchService {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.searchService.mongodb");

	protected AuthenticationService authenticationService;
	protected AuditService auditService;
	protected MessageSearchService messageSearchService;

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {

		try {
			SearchResult result = messageSearchService.search(fields);
			result.setExecutedBy(authenticationService.getUserName());
			return result;
		} catch (AuthenticationException e) {
			throw new MessageSearchException(
					"Could not get authenticated user for search", e);
		} finally {
			try {
				auditService.addEvent(AuditService.SEARCH_PERFORMED,
						authenticationService.getUserName(), fields.toString());
			} catch (AuditServiceException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("could not audit user search request", e);
			} catch (AuthenticationException e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("could not get user for audit log", e);
			}
		}
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(
			AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public MessageSearchService getMessageSearchService() {
		return messageSearchService;
	}

	public void setMessageSearchService(
			MessageSearchService messageSearchService) {
		this.messageSearchService = messageSearchService;
	}

	@Override
	public Future<SearchResult> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

}
