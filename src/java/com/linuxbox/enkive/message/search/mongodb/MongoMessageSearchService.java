package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.CC;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.DATE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MAIL_FROM;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.RCPT_TO;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.SUBJECT;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.TO;
import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;
import static com.linuxbox.enkive.search.Constants.CONTENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.search.Constants.RECIPIENT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SENDER_PARAMETER;
import static com.linuxbox.enkive.search.Constants.SUBJECT_PARAMETER;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.message.search.AbstractMessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoMessageSearchService extends AbstractMessageSearchService {

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.searchService.mongodb");

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;

	public MongoMessageSearchService(Mongo m, String dbName, String collName) {
		this.m = m;
		messageDb = m.getDB(dbName);
		messageColl = messageDb.getCollection(collName);
	}

	public Set<String> searchImpl(HashMap<String, String> fields)
			throws MessageSearchException {
		Set<String> messageIds = new HashSet<String>();

		BasicDBObject query = new BasicDBObject();

		for (String searchField : fields.keySet()) {
			if (fields.get(searchField) == null
					|| fields.get(searchField).isEmpty()) {
				// Do Nothing
			} else if (searchField.equals(SENDER_PARAMETER)) {
				// Needs to match MAIL_FROM OR FROM
				BasicDBList senderQuery = new BasicDBList();
				senderQuery.add(new BasicDBObject(MAIL_FROM, fields
						.get(searchField)));
				senderQuery
						.add(new BasicDBObject(FROM, fields.get(searchField)));
				query.put("$or", senderQuery);
			} else if (searchField.equals(RECIPIENT_PARAMETER)) {
				// Needs to match TO OR CC OR RCPTO
				BasicDBList receiverQuery = new BasicDBList();
				receiverQuery.add(new BasicDBObject(RCPT_TO, fields
						.get(searchField)));
				receiverQuery
						.add(new BasicDBObject(TO, fields.get(searchField)));
				receiverQuery
						.add(new BasicDBObject(CC, fields.get(searchField)));
				query.put("$or", receiverQuery);
			} else if (searchField.equals(DATE_EARLIEST_PARAMETER)
					|| searchField.equals(DATE_LATEST_PARAMETER)) {
				BasicDBObject dateQuery = new BasicDBObject();
				if (fields.containsKey(DATE_EARLIEST_PARAMETER) && fields
						.get(DATE_EARLIEST_PARAMETER) != null) {
					try{
						Date dateEarliest = NUMERIC_SEARCH_FORMAT.parse(fields
								.get(DATE_EARLIEST_PARAMETER));
						dateQuery.put("$gte", dateEarliest);
					} catch (ParseException e) {
						logger.warn("Could not parse earliest date submitted to search - " + fields
								.get(DATE_EARLIEST_PARAMETER));
					}
				}
				if (fields.containsKey(DATE_LATEST_PARAMETER) && fields
						.get(DATE_LATEST_PARAMETER) != null) {
					try {
						Date dateLatest = NUMERIC_SEARCH_FORMAT.parse(fields
								.get(DATE_LATEST_PARAMETER));
						dateQuery.put("$lte", dateLatest);
					} catch (ParseException e) {
						logger.warn("Could not parse latest date submitted to search - " + fields
								.get(DATE_LATEST_PARAMETER));
					}
				}
				query.put(DATE, dateQuery);
			} else if (searchField.equals(SUBJECT_PARAMETER)) {
				Pattern subjectRegex = Pattern.compile(fields.get(searchField),
						Pattern.CASE_INSENSITIVE);
				query.put(SUBJECT, subjectRegex);
			} else if (searchField.equals(CONTENT_PARAMETER)) {
				try {
					List<String> attachments = docSearchService.search(fields
							.get(searchField));
					BasicDBList attachmentQuery = new BasicDBList();
					for (String attachment : attachments)
						attachmentQuery.add(new BasicDBObject(
								ATTACHMENT_ID_LIST, attachment));
					query.put("$or", attachmentQuery);
					// Need to search attachment ids field here, separated by OR
				} catch (DocSearchException e) {
					throw new MessageSearchException(
							"Exception occurred searching Content", e);
				}
			}
		}

		DBCursor results = messageColl.find(query);
		while (results.hasNext()) {
			DBObject message = results.next();
			messageIds.add((String) message.get("_id"));
		}

		return messageIds;
	}
}
