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

import com.linuxbox.enkive.exception.CannotGetPermissionsException;
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
		try {
			if (permService.isAdmin()) {
				return query;
			} else {
				BasicDBList addressesQuery = new BasicDBList();

				Collection<String> addresses = permService
						.canReadAddresses(permService.getCurrentUsername());
				if (addresses.isEmpty()) {
					query.clear();
					query.put("_id", "");
					return query;
				}

				// Needs to match MAIL_FROM OR FROM
				BasicDBList senderQuery = new BasicDBList();
				if (fields.keySet().contains(SENDER_PARAMETER)
						&& fields.get(SENDER_PARAMETER) != null
						&& !fields.get(SENDER_PARAMETER).isEmpty()) {
					senderQuery.add(new BasicDBObject(MAIL_FROM, fields
							.get(SENDER_PARAMETER)));
					senderQuery.add(new BasicDBObject(FROM, fields
							.get(SENDER_PARAMETER)));
				}

				for (String address : addresses) {
					senderQuery.add(new BasicDBObject(MAIL_FROM, address));
					senderQuery.add(new BasicDBObject(FROM, address));
				}
				addressesQuery.add(new BasicDBObject("$or", senderQuery));

				BasicDBList receiverQuery = new BasicDBList();
				if (fields.keySet().contains(RECIPIENT_PARAMETER)
						&& fields.get(RECIPIENT_PARAMETER) != null
						&& !fields.get(RECIPIENT_PARAMETER).isEmpty()) {
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
				addressesQuery.add(new BasicDBObject("$or", receiverQuery));
				query.put("$and", addressesQuery);

				return query;
			}
		} catch (CannotGetPermissionsException e) {
			throw new MessageSearchException(
					"Could not get permissions for current user", e);
		}
	}

}
