package com.linuxbox.enkive.message.retention.mongodb;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.retention.AbstractMessageRetentionPolicyEnforcer;
import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;

public class MongoMessageRetentionPolicyEnforcer extends
		AbstractMessageRetentionPolicyEnforcer {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.message.retentionEnforcer");

	MessageSearchService searchService;

	MongoMessageRetentionPolicyEnforcer(MessageSearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	public void enforceMessageRetentionPolicies() {
		try {
			SearchResult result = searchService.search(retentionPolicy
					.retentionPolicyCriteriaToSearchFields());
			System.out.println(result.getMessageIds().size());
		} catch (MessageSearchException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
		}
	}

}
