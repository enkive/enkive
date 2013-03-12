/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/*
 * 
 */
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
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

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
