package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_TYPE;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ARCHIVE_TIME;
import static com.linuxbox.enkive.statistics.StatsConstants.SIMPLE_DATE;

import java.util.Date;
import java.util.HashMap;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.GathererMessageSearchService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;

public class MongoGathererMessageSearchService extends
		MongoMessageSearchService implements GathererMessageSearchService {

	public MongoGathererMessageSearchService(Mongo m, String dbName,
			String collName) {
		super(m, dbName, collName);
	}

	@Override
	public int getNumberOfMessages(Date startDate, Date endDate)
			throws MessageSearchException {
		String dateEarliest = new StringBuilder(SIMPLE_DATE.format(startDate))
				.toString();
		String dateLatest = new StringBuilder(SIMPLE_DATE.format(endDate))
				.toString();

		HashMap<String, String> fields = new HashMap<String, String>();
		fields.put(DATE_EARLIEST_PARAMETER, dateEarliest);
		fields.put(DATE_LATEST_PARAMETER, dateLatest);
		fields.put(DATE_TYPE, ARCHIVE_TIME);

		BasicDBObject query = buildQueryObject(fields);
		DBCursor results = messageColl.find(query);
		
		int numOfMessages = results.count();
		results.close();
		return numOfMessages;
	}

}
