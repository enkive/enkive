/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.statistics.gathering.mongodb;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_NUM;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_ATTACH_SIZE;
import static com.linuxbox.enkive.statistics.VarsMaker.createMap;
import static com.linuxbox.enkive.statistics.consolidation.ConsolidationConstants.CONSOLIDATION_AVG;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_LENGTH;
import static com.linuxbox.enkive.statistics.gathering.mongodb.MongoConstants.MONGO_UPLOAD_DATE;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.statistics.gathering.AbstractGatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoStatsAttachmentsGatherer extends AbstractGatherer {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.statistics.gathering.StatsMongoAttachmentsGatherer");
	protected Mongo m;
	protected DB db;
	protected DBCollection attachmentsColl;

	public MongoStatsAttachmentsGatherer(Mongo m, String dbName,
			String attachmentsColl, String gathererName, String humanName,
			List<String> keys) throws GathererException {
		super(gathererName, humanName, keys);
		this.m = m;
		this.db = m.getDB(dbName);
		this.attachmentsColl = db.getCollection(attachmentsColl + ".files");
	}

	@Override
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		return null;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		Map<String, Object> intervalMap = createMap();
		Map<String, Object> query = createMap();
		Map<String, Object> innerQuery = createMap();
		innerQuery.put("$gte", startTimestamp);
		innerQuery.put("$lt", endTimestamp);
		query.put(MONGO_UPLOAD_DATE, innerQuery);
		long dataByteSz = 0;
		DBCursor dataCursor = attachmentsColl.find(new BasicDBObject(query));

		for (DBObject obj : dataCursor) {
			dataByteSz += (Long) (obj.get(MONGO_LENGTH));
		}
		Map<String, Object> innerNumAttach = createMap();
		innerNumAttach.put(CONSOLIDATION_AVG, dataCursor.count());

		long avgAttSz = 0;
		if (dataCursor.count() != 0) {
			avgAttSz = dataByteSz / dataCursor.count();
		}

		intervalMap.put(STAT_ATTACH_SIZE, avgAttSz);
		intervalMap.put(STAT_ATTACH_NUM, dataCursor.count());

		return intervalMap;
	}
}
