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
package com.linuxbox.enkive.tool.mongodb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Reader;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.mongodb.MongoRetrieverService;
import com.mongodb.Mongo;

/*
 * Program to retrieve (reconstitute) full messages from the data store, keyed by
 * their unique identifier.
 * 
 * There's a helper script to run it in ENKIVE_HOME/scripts/enkive-msg-retrieve.sh
 * For example:
 * 
 *     ./enkive-doc-retrieve.sh 0576cf251ad13efdd5baa00ccad339c426fc9497
 * 
 */

public class MongoDBMsgRetriever {
	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String DATABASE_NAME = "enkive";

	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String EMAIL_COLLECTION_NAME = "emailMessages";
	
	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String GRIDFS_COLLECTION_NAME = "fs";

	// private final static String OUTPUT_DIR = "/tmp"; // command line?
	private final static String OUTPUT_DIR = ".";

	protected final static String TEMP_FILENAME_ROOT = "somefile";

	protected static enum StoreHow {
		RAW, RECONSTITUTED
	}

	private static void storeAsFile(Message msg, String id, StoreHow how) {

		File file = null;
		String msgStr = "";
		
		try {
			File OutputDir = new File(OUTPUT_DIR);

			switch (how) {
			case RAW:
				file = new File(OutputDir, id + "-raw" + ".out");
				msgStr = msg.getRawEmail();
				break;
			case RECONSTITUTED:
				file = new File(OutputDir, id + "-reconstituted" + ".out");
				msgStr = msg.getReconstitutedEmail();
				break;
			}
			
			FileWriter output = new FileWriter(file);
			output.write(msgStr);
			output.close();

			System.out.println("retrieved " + id + " " + how
					+ " to " + file.getCanonicalPath());
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
	}

	public static String getIdentifierOpt(String[] args) {
		String identifier = args[0];

		return identifier;
	}

	public static void main(String[] args) {
		System.out.println("Starting");
		
		MongoRetrieverService retriever = null;
		MongoGridDocStoreService docStoreService = null;

		try {
			Mongo m = new Mongo();
			retriever = new MongoRetrieverService(m, DATABASE_NAME, EMAIL_COLLECTION_NAME);
			
			docStoreService = new ConvenienceMongoGridDocStoreService(
					DATABASE_NAME, GRIDFS_COLLECTION_NAME);
			docStoreService.startup();
			
			retriever.setDocStoreService(docStoreService);

			String identifier = getIdentifierOpt(args);
			if (identifier == null) {
				System.out.println("usage: program <identifier>");
				return;
			}

			System.out.println("Try retrieve message with id " + identifier);
			Message msg = retriever.retrieve(identifier);
			
			storeAsFile(msg, identifier, StoreHow.RAW);
			storeAsFile(msg, identifier, StoreHow.RECONSTITUTED);
			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		} finally {
			// empty
		}

		System.out.println("Done");
	}
}
