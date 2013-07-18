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
package com.linuxbox.enkive.message.retention.mongodb;

import static com.linuxbox.enkive.search.Constants.LIMIT_PARAMETER;

import java.util.HashMap;

import com.linuxbox.enkive.message.retention.MessageRetentionPolicyEnforcer;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;

public class MongodbMessageRetentionPolicyEnforcer extends
		MessageRetentionPolicyEnforcer {

	@Override
	public void enforceMessageRetentionPolicies() {
		try {
			MongoMessageSearchService mongoSearchService = (MongoMessageSearchService) searchService;

			HashMap<String, String> retentionPolicyCriteria = retentionPolicy
					.getRetentionPolicyCriteria();
			retentionPolicyCriteria.remove(LIMIT_PARAMETER);
			retentionPolicy.setRetentionPolicyCriteria(retentionPolicyCriteria);
			for (String messageId : mongoSearchService
					.searchImpl(retentionPolicy
							.retentionPolicyCriteriaToSearchFields()).values()) {
				if (messageArchivingService.removeMessage(messageId))
					LOGGER.info("Message Removed by retention policy: "
							+ messageId);
				if (this.shutdown)
					break;
			}
		} catch (MessageSearchException e) {
			LOGGER.warn(
					"Error searching for messages while enforcing retention policy",
					e);
		}
	}
}
