package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.ARCHIVE_TIME;
import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.DATE;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.DATE_TYPE;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;
import static com.linuxbox.enkive.search.Constants.SPECIFIC_SEARCH_FORMAT;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class DateQueryBuilder extends AbstractMongoMessageQueryBuilder {
	public static Date parseDate(String dateStr) throws ParseException {
		try {
			return SPECIFIC_SEARCH_FORMAT.parse(dateStr);
		} catch (ParseException e1) {
			return NUMERIC_SEARCH_FORMAT.parse(dateStr);
		}
	}

	protected void buildLatest(Map<String, String> fields,
			BasicDBObjectBuilder builder) {
		final String dateLatestStr = fields.get(DATE_LATEST_PARAMETER);
		if (null == dateLatestStr || dateLatestStr.isEmpty()) {
			return;
		}

		try {
			Date latestDate = parseDate(dateLatestStr);

			// move to next date, so we can use less-than
			Calendar c = Calendar.getInstance();
			c.setTime(latestDate);
			c.add(Calendar.DATE, 1);
			latestDate = c.getTime();

			builder.add("$lt", latestDate);
		} catch (ParseException e) {
			LOGGER.warn("Could not parse latest date submitted to search \""
					+ dateLatestStr + "\"");
		}
	}

	protected void buildEarliest(Map<String, String> fields,
			BasicDBObjectBuilder builder) {
		final String dateEarliestStr = fields.get(DATE_EARLIEST_PARAMETER);
		if (null == dateEarliestStr || dateEarliestStr.isEmpty()) {
			return;
		}

		try {
			Date dateEarliest = parseDate(dateEarliestStr);
			builder.add("$gte", dateEarliest);
		} catch (ParseException e) {
			LOGGER.warn("Could not parse earliest date submitted to search \""
					+ dateEarliestStr + "\"");
		}
	}

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields) {
		BasicDBObjectBuilder result = new BasicDBObjectBuilder();

		buildEarliest(fields, result);
		buildLatest(fields, result);

		if (result.isEmpty()) {
			return null;
		} else if (fields.containsKey(DATE_TYPE)
				&& fields.get(DATE_TYPE).equals(ARCHIVE_TIME)) {
			return new BasicDBObject(ARCHIVE_TIME, result.get());
		} else {
			return new BasicDBObject(DATE, result.get());
		}
	}
}