package com.linuxbox.enkive.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.EncodedChainedDocument;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.InMemorySHA1Document;
import com.linuxbox.enkive.docstore.SimpleDocument;
import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.util.StreamConnector;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class TestMongoGridDocStore {
	static DocStoreService docService;
	static Set<String> indexSet = new HashSet<String>();

	private static void archive(String content) throws DocStoreException {
		String index = docService.store(new SimpleDocument(content,
				"text/plain", "txt"));
		indexSet.add(index);
	}

	private static void archiveAll() {
		final String duplicated = "This is another test.";
		final String[] documents = { "This is a test.", duplicated,
				"This is a third test.", "This is a fourth test.", duplicated };

		for (String doc : documents) {
			try {
				archive(doc);
			} catch (DocStoreException e) {
				System.err.println(e);
			}
		}
	}

	private static void retrieveAll() {
		for (String index : indexSet) {
			System.out.print("retrieving " + index + ": ");
			try {
				Document d = docService.retrieve(index);
				String s = new String(d.getContentBytes());
				System.out.println(s);
			} catch (DocumentNotFoundException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private final static String inputDir = "../..";

	static class FileRecord {
		String name;
		String mimeType;
		String suffix;
		String encoding;
		String characterSet;

		FileRecord(String a, String b, String c, String d, String e) {
			this.name = a;
			this.mimeType = b;
			this.suffix = c;
			this.encoding = d;
			this.characterSet = e;
		}
		
		FileRecord(String a, String b, String c, String d) {
			this(a, b, c, d, null);
		}
	}

	private final static FileRecord[] encodedFiles = {
			new FileRecord("1-b64.pdf", "application/pdf", "pdf",
					MimeUtil.ENC_BASE64),
			new FileRecord("2-b64.pdf", "application/pdf", "pdf",
					MimeUtil.ENC_BASE64),
			new FileRecord("3-qp.txt", "text/plain", "txt",
					MimeUtil.ENC_QUOTED_PRINTABLE, "windows-1252") };

	private final static Set<String> encodedIndexSet = new HashSet<String>();

	private static void archiveEncoded() {
		for (FileRecord fileRec : encodedFiles) {
			try {
				File f = new File(inputDir + "/" + fileRec.name);
				FileInputStream fileStream = new FileInputStream(f);
				
				Document d = new InMemorySHA1Document(fileRec.mimeType,
						fileRec.suffix, fileStream);
				EncodedDocument ed = new EncodedChainedDocument(
						fileRec.encoding, d);
				encodedIndexSet.add(ed.getIdentifier());
				docService.store(ed);

				fileStream.close();
				
				System.out.println("archived encoded " + ed.getIdentifier());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private static void retrieveEncoded() {
		int counter = 0;
		for (String identifier : encodedIndexSet) {
			FileOutputStream fileStream = null;
			++counter;
			try {
				Document d = docService.retrieve(identifier);
				File f = new File(inputDir + "/" + counter + "."
						+ d.getSuffix());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getContentStream(),
						fileStream);
				fileStream.close();
				System.out.println("wrote " + d.getIdentifier() + " to " + f.getCanonicalPath());
			} catch (Throwable e) {
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
	}

	private static void dropCollection(DB db, String collection) {
		if (db.collectionExists(collection)) {
			db.getCollection(collection).drop();
		}
	}

	private static void dropGridFSCollections(String dbName, String bucket)
			throws UnknownHostException {
		DB db = new Mongo().getDB(dbName);
		dropCollection(db, bucket + ".files");
		dropCollection(db, bucket + ".chunks");
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		try {
			dropGridFSCollections("enkive", "fs");
			docService = new MongoGridDocStoreService("enkive", "fs");

			archiveAll();
			retrieveAll();

			archiveEncoded();
			retrieveEncoded();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}
}
