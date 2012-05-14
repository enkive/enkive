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
					.searchImpl(retentionPolicy.retentionPolicyCriteriaToSearchFields())) {
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
