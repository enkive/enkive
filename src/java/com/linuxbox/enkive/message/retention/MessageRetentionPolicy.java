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
/*
 * 
 */
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
