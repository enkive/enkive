package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.search.Constants.PERMISSIONS_RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.PERMISSIONS_SENDER_PARAMETER;

import java.util.Map;
import java.util.regex.Pattern;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This class is an inefficient mess, in part due to what's expected to come in.
 * Why do we have separate PERMISSIONS_SENDER_PARAMETER and
 * PERMISSIONS_RECIPIENT_PARAMETER?
 * 
 * We assume that there's a list of senders and recipients and one of them must
 * match. Basically the assumption is that a given person, with one of his/her
 * email addresses, either sent or received the message. So we should be able to
 * find any one match b/w all the addresses and all the components of the email
 * message.
 */
public class PermissionsQueryBuilder extends AbstractMongoMessageQueryBuilder {
	protected static final String[] SENDER_QUERIES = { MAIL_FROM, FROM };
	protected static final String[] RECIPIENT_QUERIES = { RCPT_TO, TO, CC };

	protected void addPermissionClauses(Map<String, String> fields, String key,
			String[] queries, BasicDBList terms) {
		final String addressListStr = fields.get(key);
		if (null == addressListStr) {
			return;
		}

		final String[] addresses = addressListStr.trim().split(";");
		for (String address : addresses) {
			address = address.trim();
			if (!address.isEmpty()) {
				final Pattern addressRegex = Pattern.compile(address,
						Pattern.CASE_INSENSITIVE);
				for (String query : queries) {
					terms.add(new BasicDBObject(query, addressRegex));
				}
			}
		}
	}

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields)
			throws EmptySearchResultsException, MessageSearchException {
		BasicDBList terms = new BasicDBList();

		addPermissionClauses(fields, PERMISSIONS_SENDER_PARAMETER,
				SENDER_QUERIES, terms);
		addPermissionClauses(fields, PERMISSIONS_RECIPIENT_PARAMETER,
				RECIPIENT_QUERIES, terms);

		if (terms.isEmpty()) {
			return null;
		} else {
			return new BasicDBObject("$or", terms);
		}
	}
}
