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
 *******************************************************************************/
package com.linuxbox.enkive.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* XXX eventually may want this */
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;

/* XXX don't need this? */
import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docsearch.contentanalyzer.tika.TikaContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.FileSystemDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StringDocument;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.mongogrid.Constants;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.util.StreamConnector;
import com.linuxbox.util.mongodb.Dropper;

/*
 * Program to retrieve arbitrary objects from the data store, keyed by their
 * cryptographic identifier.  Borrows extensively from unit test code.
 *
 * export enkiveHome=/opt/enkive
 * export enkiveBuild=/home/matt/enkive/enkive2/
 * 
 * java -cp $enkiveBuild/bin:$enkiveHome/enkive.jar:$enkiveHome/lib/*: \
 *     com.linuxbox.enkive.testing.TestMongoDocRetrieval \
 *     3ba1489afcee26d3e0187777392dcfa5b33b1e84
 * Starting
 * retrieving 3ba1489afcee26d3e0187777392dcfa5b33b1e84 ENCODED to /tmp/3ba1489afcee26d3e0187777392dcfa5b33b1e84_enc.LinuxBoxW_P.120507.gif
 * retrieving 3ba1489afcee26d3e0187777392dcfa5b33b1e84 DECODED to /tmp/3ba1489afcee26d3e0187777392dcfa5b33b1e84_dec.LinuxBoxW_P.120507.gif
 * Done
 * 
 */

public class TestMongoDocRetrieval {

	private final static String DATABASE_NAME = "enkive";
	private final static String GRIDFS_COLLECTION_NAME = "fs";
	private final static String outputDir = "/tmp"; /* cmdline? */
	
	enum store_how {
		ENCODED, DECODED
	}

	static MongoGridDocStoreService docStoreService;

	private static Document retrieveEncoded(String identifier) {
		Document d = null;
		try {
			d = docStoreService.retrieve(identifier);
		}
		catch (Throwable e) {
			e.printStackTrace();
		} finally {
			/* nothing */
		}		
		return (d);
	}

	private static void storeAsFile(Document d, store_how how) {
		FileOutputStream fileStream = null;
		File f = null;
		try {
			switch (how) {
			case ENCODED:
				f = new File(outputDir + "/" + d.getFilename() + "_enc."
						+ d.getFileExtension());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getEncodedContentStream(),
						fileStream);
				break;
			case DECODED:
			default:
				f = new File(outputDir + "/" + d.getFilename() + "_dec."
						+ d.getFileExtension());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getDecodedContentStream(),
						fileStream);
				break;
			}
			fileStream.close();
			System.out.println("retrieving " + d.getFilename() + " " + how + " to "
					+ f.getCanonicalPath());
		}
		catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileStream != null) {
					fileStream.close();
				}
			} catch (Exception e) {
				// empty
			}
		}	
		
	}
	
	public static String getIdentifierOpt(String[] args) {
		
		/* XXX for now, just skip it */
		String identifier = args[0];
		
		return (identifier);
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		try {
			docStoreService = new ConvenienceMongoGridDocStoreService(
					DATABASE_NAME, GRIDFS_COLLECTION_NAME);
			docStoreService.startup();

			String identifier = getIdentifierOpt(args);
			if (identifier == null) {
				System.out.println("usage:  program <identifier>");
				return;
			}
			Document d = retrieveEncoded(identifier);
			
			storeAsFile(d, store_how.ENCODED);
			storeAsFile(d, store_how.DECODED);
			
			/* XXX need to release d? */

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (docStoreService != null) {
					docStoreService.shutdown();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Done");
	}
}
