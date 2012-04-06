package com.linuxbox.enkive.message.retention;

import static com.linuxbox.enkive.message.retention.Constants.RETENTION_PERIOD;
import static com.linuxbox.enkive.search.Constants.DATE_LATEST_PARAMETER;
import static com.linuxbox.enkive.search.Constants.LIMIT_PARAMETER;
import static com.linuxbox.enkive.search.Constants.NUMERIC_SEARCH_FORMAT;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MessageRetentionPolicy {

	protected HashMap<String, String> retentionPolicyCriteria;

	public HashMap<String, String> getRetentionPolicyCriteria() {
		return retentionPolicyCriteria;
	}

	public void setRetentionPolicyCriteria(
			HashMap<String, String> retentionPolicyCriteria) {
		this.retentionPolicyCriteria = retentionPolicyCriteria;
	}

	public HashMap<String, String> retentionPolicyCriteriaToSearchFields() {
		HashMap<String, String> retentionSearchFields = new HashMap<String, String>();
		if (retentionPolicyCriteria.get(RETENTION_PERIOD) != null
				&& !retentionPolicyCriteria.get(RETENTION_PERIOD).isEmpty()) {
			String retentionPeriodString = retentionPolicyCriteria
					.get(RETENTION_PERIOD);
			int retentionPeriod = Integer.parseInt(retentionPeriodString);
			Calendar retentionCal = Calendar.getInstance();
			retentionCal.add(Calendar.DATE, (-1 * retentionPeriod));
			Date retentionDate = retentionCal.getTime();
			retentionSearchFields.put(DATE_LATEST_PARAMETER,
					NUMERIC_SEARCH_FORMAT.format(retentionDate));

		}
		if (retentionPolicyCriteria.get(LIMIT_PARAMETER) != null
				&& !retentionPolicyCriteria.get(LIMIT_PARAMETER).isEmpty()) {
			retentionSearchFields.put(LIMIT_PARAMETER,
					retentionPolicyCriteria.get(LIMIT_PARAMETER));
		}
		return retentionSearchFields;
	}

}
