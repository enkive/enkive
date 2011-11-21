package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;

import java.util.Collection;
import java.util.HashMap;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.permissions.PermissionService;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Mongo;

public class PermissionEnforcingMongoSearchService extends
		MongoMessageSearchService {

	PermissionService permService;

	public PermissionEnforcingMongoSearchService(PermissionService permService,
			Mongo m, String dbName, String collName) {
		super(m, dbName, collName);
		this.permService = permService;
	}

	@Override
	protected BasicDBObject buildQueryObject(HashMap<String, String> fields)
			throws MessageSearchException {

		BasicDBObject query = super.buildQueryObject(fields);

		if (permService.isAdmin()) {
			return query;
		} else {
			// Needs to match MAIL_FROM OR FROM
			BasicDBList senderQuery = new BasicDBList();
			if (fields.keySet().contains(SENDER_PARAMETER)) {
				senderQuery.add(new BasicDBObject(MAIL_FROM, fields
						.get(SENDER_PARAMETER)));
				senderQuery.add(new BasicDBObject(FROM, fields
						.get(SENDER_PARAMETER)));
			}
			Collection<String> addresses = permService
					.canReadAddresses(permService.getCurrentUsername());
			for (String address : addresses) {
				senderQuery.add(new BasicDBObject(MAIL_FROM, address));
				senderQuery.add(new BasicDBObject(FROM, address));
			}
			query.put("$or", senderQuery);

			BasicDBList receiverQuery = new BasicDBList();
			if (fields.keySet().contains(RECIPIENT_PARAMETER)) {
				receiverQuery.add(new BasicDBObject(RCPT_TO, fields
						.get(RECIPIENT_PARAMETER)));
				receiverQuery.add(new BasicDBObject(TO, fields
						.get(RECIPIENT_PARAMETER)));
				receiverQuery.add(new BasicDBObject(CC, fields
						.get(RECIPIENT_PARAMETER)));
			}
			for (String address : addresses) {
				receiverQuery.add(new BasicDBObject(RCPT_TO, address));
				receiverQuery.add(new BasicDBObject(TO, address));
				receiverQuery.add(new BasicDBObject(CC, address));
			}
			query.put("$or", receiverQuery);

			return query;
		}
	}

}
