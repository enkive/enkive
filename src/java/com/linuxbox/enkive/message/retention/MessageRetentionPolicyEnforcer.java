/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/
/*
 * 
 */
package com.linuxbox.enkive.message.retention;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;
import static com.linuxbox.enkive.message.retention.Constants.RETENTION_PERIOD;

public class MessageRetentionPolicyEnforcer {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.messageRetentionPolicyEnforcer");

	protected boolean shutdown = false;
	
	protected MessageSearchService searchService;
	protected MessageArchivingService messageArchivingService;
	protected MessageRetentionPolicy retentionPolicy;

	public MessageRetentionPolicyEnforcer() {

	}

	public void enforceMessageRetentionPolicies() {

		try {
			SearchResult result;
			if (Integer.parseInt(retentionPolicy.getRetentionPolicyCriteria()
					.get(RETENTION_PERIOD)) > 0)
				do {
					result = getMessagesForDeletion();
					for (String messageId : result.getMessageIds()) {
						if (messageArchivingService.removeMessage(messageId))
							LOGGER.info("Message Removed by retention policy: "
									+ messageId);
					}
				} while (result.getMessageIds().size() > 0);

		} catch (MessageSearchException e) {
			LOGGER.warn(
					"Error searching for messages while enforcing retention policy",
					e);
		}
	}

	protected SearchResult getMessagesForDeletion()
			throws MessageSearchException {
		return searchService.search(retentionPolicy
				.retentionPolicyCriteriaToSearchFields());
	}

	public MessageSearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	public MessageRetentionPolicy getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(MessageRetentionPolicy retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	public MessageArchivingService getMessageArchivingService() {
		return messageArchivingService;
	}

	public void setMessageArchivingService(
			MessageArchivingService messageArchivingService) {
		this.messageArchivingService = messageArchivingService;
	}

	public void shutdown(){
		this.shutdown = true;
	}
	
}
