/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.retriever;

import static com.linuxbox.enkive.message.retention.Constants.RETENTION_PERIOD;

import java.text.ParseException;
import java.util.Date;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentReadData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;
import com.linuxbox.enkive.message.retention.MessageRetentionPolicy;
import com.linuxbox.enkive.search.Constants;

public class RetentionPolicyEnforcingMessageRetrieverService extends
		AbstractRetrieverService implements MessageRetrieverService {

	MessageRetentionPolicy retentionPolicy;
	MessageRetrieverService retrieverService;

	@Override
	public MessageSummary retrieveSummary(String messageId)
			throws CannotRetrieveException {
		MessageSummary message = retrieverService.retrieveSummary(messageId);
		if (Integer.parseInt(retentionPolicy.getRetentionPolicyCriteria().get(
				RETENTION_PERIOD)) <= 0)
			return message;
		Date messageDate = message.getDate();
		try {
			Date retentionPolicyDate = Constants.NUMERIC_SEARCH_FORMAT
					.parse(retentionPolicy
							.retentionPolicyCriteriaToSearchFields().get(
									Constants.DATE_LATEST_PARAMETER));
			if (messageDate.after(retentionPolicyDate))
				return message;
			else
				throw new CannotRetrieveException(
						"Attempted message for retrieval is after retention policy date");
		} catch (ParseException e) {
			throw new CannotRetrieveException(
					"Could not parse retention policy date for retrieval.", e);
		}
	}

	@Override
	public Message retrieve(String messageUUID) throws CannotRetrieveException {
		Message message = retrieverService.retrieve(messageUUID);
		if (Integer.parseInt(retentionPolicy.getRetentionPolicyCriteria().get(
				RETENTION_PERIOD)) <= 0)
			return message;
		Date messageDate = message.getDate();
		try {
			Date retentionPolicyDate = Constants.NUMERIC_SEARCH_FORMAT
					.parse(retentionPolicy
							.retentionPolicyCriteriaToSearchFields().get(
									Constants.DATE_LATEST_PARAMETER));
			if (messageDate.after(retentionPolicyDate))
				return message;
			else
				throw new CannotRetrieveException(
						"Attempted message for retrieval is after retention policy date");
		} catch (ParseException e) {
			throw new CannotRetrieveException(
					"Could not parse retention policy date for retrieval.", e);
		}
	}

	@Override
	public EncodedContentReadData retrieveAttachment(String attachmentUUID)
			throws CannotRetrieveException {
		return retrieverService.retrieveAttachment(attachmentUUID);
	}

	public MessageRetentionPolicy getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(MessageRetentionPolicy retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	public MessageRetrieverService getRetrieverService() {
		return retrieverService;
	}

	public void setRetrieverService(MessageRetrieverService retrieverService) {
		this.retrieverService = retrieverService;
	}

}
