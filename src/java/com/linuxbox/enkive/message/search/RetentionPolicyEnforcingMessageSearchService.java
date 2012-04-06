package com.linuxbox.enkive.message.search;

import static com.linuxbox.enkive.message.retention.Constants.RETENTION_PERIOD;
import static com.linuxbox.enkive.search.Constants.DATE_EARLIEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.message.retention.MessageRetentionPolicy;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;

public class RetentionPolicyEnforcingMessageSearchService implements
		MessageSearchService {

	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.searchService");

	protected MessageSearchService messageSearchService;
	protected MessageRetentionPolicy messageRetentionPolicy;

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {

		HashMap<String, String> searchFields = addRetentionPolicyToFields(fields);
		return messageSearchService.search(searchFields);
	}

	@Override
	public Future<SearchResult> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {
		throw new MessageSearchException("Unimplemented");
	}

	public MessageSearchService getMessageSearchService() {
		return messageSearchService;
	}

	public void setMessageSearchService(
			MessageSearchService messageSearchService) {
		this.messageSearchService = messageSearchService;
	}

	public MessageRetentionPolicy getMessageRetentionPolicy() {
		return messageRetentionPolicy;
	}

	public void setMessageRetentionPolicy(
			MessageRetentionPolicy messageRetentionPolicy) {
		this.messageRetentionPolicy = messageRetentionPolicy;
	}

	private HashMap<String, String> addRetentionPolicyToFields(
			HashMap<String, String> searchFields) {
		HashMap<String, String> retentionFields = messageRetentionPolicy
				.getRetentionPolicyCriteria();

		if (retentionFields.get(RETENTION_PERIOD) != null
				&& !retentionFields.get(RETENTION_PERIOD).isEmpty()) {

			String retentionPeriodString = retentionFields
					.get(RETENTION_PERIOD);
			int retentionPeriod = Integer.parseInt(retentionPeriodString);
			Calendar retentionCal = Calendar.getInstance();
			retentionCal.add(Calendar.DATE, (-1 * retentionPeriod));
			Date retentionDate = retentionCal.getTime();

			if (searchFields.get(DATE_EARLIEST_PARAMETER) != null
					&& !searchFields.get(DATE_EARLIEST_PARAMETER).isEmpty()) {
				try {
					Date searchDate = NUMERIC_SEARCH_FORMAT.parse(searchFields
							.get(DATE_EARLIEST_PARAMETER));
					if (retentionDate.after(searchDate)) {
						searchFields.put(DATE_EARLIEST_PARAMETER,
								NUMERIC_SEARCH_FORMAT.format(retentionDate));
					}
				} catch (ParseException e) {
					searchFields.put(DATE_EARLIEST_PARAMETER,
							NUMERIC_SEARCH_FORMAT.format(retentionDate));
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("Could not parse earliest date submitted to search - "
								+ searchFields.get(DATE_EARLIEST_PARAMETER));
				}
			} else {
				searchFields.put(DATE_EARLIEST_PARAMETER,
						NUMERIC_SEARCH_FORMAT.format(retentionDate));
			}
		}
		return searchFields;

	}

}
