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

import lemurproject.indri.IndexStatus;

import org.apache.james.mime4j.util.MimeUtil;

import com.linuxbox.enkive.docsearch.SearchService;
import com.linuxbox.enkive.docsearch.contentanalyzer.tika.TikaContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docsearch.indri.IndriSearchService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.FileSystemDocument;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StringDocument;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.util.StreamConnector;
import com.mongodb.DB;
import com.mongodb.Mongo;

public class TestMongoGridDocStore {
	private static final String INDRI_REPOSITORY_PATH = "/tmp/enkive-indri";
	private static final String INDRI_TEMP_STORAGE_PATH = "/tmp/enkive-indri-tmp";
	private static final boolean DO_PUSH_INDEXING = false;

	static DocStoreService docStoreService;
	static SearchService docSearchService;
	static Set<String> indexSet = new HashSet<String>();

	static class RecordedIndexStatus extends IndexStatus {
		public int code;
		public String documentPath;
		public String error;
		public int documentsIndexed;
		public int documentsSeen;

		public void status(int code, String docPath, String error,
				int docsIndexed, int docsSeen) {
			this.code = code;
			this.documentPath = docPath;
			this.error = error;
			this.documentsIndexed = docsIndexed;
			this.documentsSeen = docsSeen;
		}

		public void display(PrintStream out) {
			System.out.println("code: " + code);
			System.out.println("document path: " + documentPath);
			System.out.println("error: " + error);
			System.out.println("documents indexed: " + documentsIndexed);
			System.out.println("documents seen: " + documentsSeen);
		}
	}

	private static void index(StoreRequestResult storageResult)
			throws DocSearchException, DocStoreException {
		if (DO_PUSH_INDEXING) {
			final String identifier = storageResult.getIdentifier();

			if (!storageResult.getAlreadyStored()) {
				docSearchService.indexDocument(identifier);
			}
		}
	}

	private static void archive(String content) throws DocStoreException,
			DocSearchException {
		StoreRequestResult result = docStoreService.store(new StringDocument(
				content, "text/plain", "txt", "7 bit"));
		index(result);
		indexSet.add(result.getIdentifier());
	}

	private static void archiveAll() {
		final String duplicated = "This is another test.";
		final String[] documents = { "This is a test.", duplicated,
				"This is a third test.", "This is a fourth test.", duplicated };

		for (String doc : documents) {
			try {
				archive(doc);
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace();
			}
		}
	}

	private static void retrieveAll() {
		for (String index : indexSet) {
			System.out.print("retrieving " + index + ": ");
			try {
				Document d = docStoreService.retrieve(index);
				BufferedReader r = new BufferedReader(new InputStreamReader(
						d.getDecodedContentStream()));
				String line;
				while ((line = r.readLine()) != null) {
					System.out.println(line);
				}
				r.close();
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

	private final static Set<String> encodedIdentifierSet = new HashSet<String>();

	private static void archiveEncoded() {
		for (FileRecord fileRec : encodedFiles) {
			try {
				FileSystemDocument d = new FileSystemDocument(inputDir + "/"
						+ fileRec.name, fileRec.mimeType, fileRec.suffix,
						fileRec.encoding);
				StoreRequestResult result = docStoreService.store(d);
				index(result);

				final String identifier = result.getIdentifier();
				encodedIdentifierSet.add(identifier);
				System.out.println("archived encoded " + identifier);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private static void retrieveEncoded() {
		int counter = 0;
		for (String identifier : encodedIdentifierSet) {
			FileOutputStream fileStream = null;
			++counter;
			try {
				Document d = docStoreService.retrieve(identifier);
				File f = new File(inputDir + "/" + counter + "."
						+ d.getFileExtension());
				fileStream = new FileOutputStream(f);
				StreamConnector.transferForeground(d.getEncodedContentStream(),
						fileStream);
				fileStream.close();
				System.out.println("wrote " + identifier + " to "
						+ f.getCanonicalPath());
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

	private static void searchFor(String query) throws DocSearchException {
		System.out.println("SEARCHING FOR: " + query);
		List<String> theSearch = docSearchService.search(query);
		if (theSearch.isEmpty()) {
			System.out.println("no search results");
		} else {
			for (String docIdentifier : theSearch) {
				System.out.println(docIdentifier);
			}
		}
	}

	private static void dropCollection(DB db, String collection) {
		if (db.collectionExists(collection)) {
			db.getCollection(collection).drop();
		}
	}

	private static void pullIndexingAndMarkAsIndexed() {
		String identifier = docStoreService.nextUnindexed();
		if (identifier != null) {
			System.out.println("indexing: " + identifier);
			try {
				docStoreService.markAsIndexed(identifier);
			} catch (DocSearchException e) {
				System.err.println(e);
			}
		} else {
			System.out.println("no documents need indexing");
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
			docStoreService = new MongoGridDocStoreService("enkive", "fs");

			docSearchService = new IndriSearchService(docStoreService,
					new TikaContentAnalyzer(), INDRI_REPOSITORY_PATH,
					INDRI_TEMP_STORAGE_PATH);

			archiveAll();
			retrieveAll();

			archiveEncoded();
			retrieveEncoded();

			// searchFor("frack");
			searchFor("#1(the question)");
			searchFor("#1(BE THAT)");
			searchFor("#1(question the)");
			searchFor("test");

			pullIndexingAndMarkAsIndexed();
			pullIndexingAndMarkAsIndexed();
			pullIndexingAndMarkAsIndexed();
			pullIndexingAndMarkAsIndexed();

			docSearchService.shutdown();
			docStoreService.shutdown();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}
}
