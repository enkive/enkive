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

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;

/*
 * Program to retrieve arbitrary objects from the data store, keyed by their
 * cryptographic identifier.  Borrows extensively from unit test code.
 * 
 * There's a helper script to run it in ENKIVE_HOME/scripts/enkive-doc-retrieve.sh
 * For example:
 * 
 *     ./enkive-doc-retrieve.sh 273ae265a2b4074092e5afac4a02fa706be208d0
 * 
 */

public class MongoDBDocRetriever {
	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String DATABASE_NAME = "enkive";

	// FIXME: Since this is defined in the spring configuration, it should be
	// retrieved from there rather than hard-coded
	private final static String GRIDFS_COLLECTION_NAME = "fs";

	// private final static String OUTPUT_DIR = "/tmp"; // command line?
	private final static String OUTPUT_DIR = ".";

	protected final static String TEMP_FILENAME_ROOT = "somefile";

	protected static enum StoreHow {
		ENCODED, DECODED, TEXT_CONVERTED
	}

	private static void storeAsFile(Document doc, StoreHow how) {
		String fileExtension = doc.getFileExtension();
		if (null != fileExtension) {
			fileExtension = "." + fileExtension;
		} else {
			// use this file extension if attempts to fix below fail
			fileExtension = ".UNKNOWN";

			// try use Apache Tika to get the best possible extension given the
			// mime type
			final String mimeTypeString = doc.getMimeType();
			System.out.println("Document claims to be of MIME type: "
					+ mimeTypeString);
			if (null != mimeTypeString) {
				try {
					final MimeType mimeType = MimeTypes.getDefaultMimeTypes()
							.getRegisteredMimeType(mimeTypeString);
					if (null != mimeType) {
						fileExtension = mimeType.getExtension();
					} else {
						System.out
								.println("Cannot determine a standard file extension for that MIME type.");
					}
				} catch (MimeTypeException e) {
					// empty; leaves fileExtension as "UNKNOWN"
				}
			}
		}

		File file = null;

		try {
			InputStream inputStream = null;
			Reader reader = null;
			File OutputDir = new File(OUTPUT_DIR);

			switch (how) {
			case TEXT_CONVERTED:
				file = new File(OutputDir, doc.getFilename() + fileExtension
						+ ".txt");
				{
					final Tika tika = new Tika();
					final Metadata metaData = new Metadata();
					metaData.set(Metadata.CONTENT_TYPE, doc.getMimeType());
					metaData.set(Metadata.CONTENT_DISPOSITION,
							TEMP_FILENAME_ROOT + doc.getFileExtension());
					reader = tika
							.parse(doc.getDecodedContentStream(), metaData);
				}
				break;

			case ENCODED:
				file = new File(OutputDir, doc.getFilename() + fileExtension
						+ "-encoded");
				inputStream = doc.getEncodedContentStream();
				break;

			case DECODED:
			default:
				file = new File(OutputDir, doc.getFilename() + fileExtension);
				inputStream = doc.getDecodedContentStream();
				break;
			}

			if (inputStream != null) {
				FileOutputStream output = new FileOutputStream(file);
				IOUtils.copy(inputStream, output);
				IOUtils.closeQuietly(inputStream);
				IOUtils.closeQuietly(output);
			} else if (reader != null) {
				FileWriter output = new FileWriter(file);
				IOUtils.copy(reader, output);
				IOUtils.closeQuietly(reader);
				IOUtils.closeQuietly(output);
			} else {
				throw new Exception("no input source");
			}

			System.out.println("retrieved " + doc.getFilename() + " " + how
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

		MongoGridDocStoreService docStoreService = null;

		try {
			docStoreService = new ConvenienceMongoGridDocStoreService(
					DATABASE_NAME, GRIDFS_COLLECTION_NAME);
			docStoreService.startup();

			String identifier = getIdentifierOpt(args);
			if (identifier == null) {
				System.out.println("usage: program <identifier>");
				return;
			}

			Document d = docStoreService.retrieve(identifier);

			storeAsFile(d, StoreHow.ENCODED);
			storeAsFile(d, StoreHow.DECODED);
			storeAsFile(d, StoreHow.TEXT_CONVERTED);
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		} finally {
			try {
				if (docStoreService != null) {
					docStoreService.shutdown();
				}
			} catch (Exception e) {
				// empty
			}
		}

		System.out.println("Done");
	}
}
