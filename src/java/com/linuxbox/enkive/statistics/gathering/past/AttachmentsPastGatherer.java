/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.statistics.gathering.past;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_ARCHIVE_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_GATHERER_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIMESTAMP;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MAX;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_MIN;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_TYPE;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.linuxbox.enkive.statistics.services.StatsClient;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class AttachmentsPastGatherer extends PastGatherer {
	DBCollection attachmentsColl;

	public AttachmentsPastGatherer(MongoClient m, String dbName,
			String attachmentsColl, String statisticsColl, String name,
			StatsClient client, int hrKeepTime, int dayKeepTime,
			int weekKeepTime, int monthKeepTime) {
		super(name, client, hrKeepTime, dayKeepTime, weekKeepTime,
				monthKeepTime);
		this.attachmentsColl = m.getDB(dbName).getCollection(
				attachmentsColl + ".files");
	}

	@PostConstruct
	public void init() {
		consolidatePastHours();
		consolidatePastDays();
		consolidatePastWeeks();
		consolidatePastMonths();
	}

	protected Map<String, Object> getConsolidatedData(Date start, Date end,
			int grain) {
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> query = new HashMap<String, Object>();
		Map<String, Object> innerQuery = new HashMap<String, Object>();
		innerQuery.put("$gte", start);
		innerQuery.put("$lt", end);
		query.put(MONGO_UPLOAD_DATE, innerQuery);
		long dataByteSz = 0;
		DBCursor dataCursor = attachmentsColl.find(new BasicDBObject(query));

		for (DBObject obj : dataCursor) {
			dataByteSz += (Long) (obj.get(MONGO_LENGTH));
		}
		Map<String, Object> innerNumAttach = new HashMap<String, Object>();
		innerNumAttach.put(CONSOLIDATION_AVG, dataCursor.count());

		Map<String, Object> innerAttachSz = new HashMap<String, Object>();

		long avgAttSz = 0;
		if (dataCursor.count() != 0) {
			avgAttSz = dataByteSz / dataCursor.count();
		}

		innerAttachSz.put(CONSOLIDATION_AVG, avgAttSz);

		Map<String, Object> innerAttArchiveSize = new HashMap<String, Object>();
		innerAttArchiveSize.put(CONSOLIDATION_AVG, attachmentsColl.count());

		Map<String, Object> dateMap = new HashMap<String, Object>();
		dateMap.put(CONSOLIDATION_MIN, start);
		dateMap.put(CONSOLIDATION_MAX, end);

		result.put(STAT_ATTACH_SIZE, innerAttachSz);
		result.put(STAT_ATTACH_NUM, innerNumAttach);
		result.put(STAT_ATTACH_ARCHIVE_SIZE, innerAttArchiveSize);
		result.put(STAT_TIMESTAMP, dateMap);
		result.put(CONSOLIDATION_TYPE, grain);
		result.put(STAT_GATHERER_NAME, gathererName);

		return result;
	}
}
