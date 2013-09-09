/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
 *******************************************************************************/
package com.linuxbox.enkive.imap.migration;

import com.linuxbox.util.dbinfo.DbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.DbMigration;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrator;

/**
 * Migrates the IMAP data from DB version 1 to 2.  Since version 2 of IMAP
 * dosen't have it's own database, this just nukes the whole collection from
 * orbit.
 */
public class ImapMigration1to2 extends DbMigration {

	public ImapMigration1to2(DbMigrator migrator) throws DbMigrationException {
		super(migrator, 1, 2);
	}

	@Override
	public void migrate(DbInfo dbInfo) throws DbMigrationException {
		LOGGER.info("Running IMAP migration 1 to 2");

		try {
			// Drop the IMAP Collection
			((MongoDbInfo)dbInfo).getCollection().drop();
		} catch (Exception e) {
			throw new DbMigrationException("Failed to migrate IMAP from 1 to 2: " + e.toString(), e);
		}
	}
}
