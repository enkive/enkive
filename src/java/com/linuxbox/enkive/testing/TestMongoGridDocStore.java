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

import com.linuxbox.enkive.docsearch.contentanalyzer.tika.TikaContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchIndexService;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
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

public class TestMongoGridDocStore {
	enum Indexing {
		PUSH, MANUAL_PULL, AUTO_PULL
	};

	private final static String DATABASE_NAME = "enkiveTest";
	private final static String GRIDFS_COLLECTION_NAME = "fsTest";

	private static final boolean DROP_DOCS_ON_STARTUP = true;
	private static final Indexing INDEXING_METHOD = Indexing.AUTO_PULL;
	private static final int INDEXING_POLL_TIME = 1;

	private static final String INDRI_REPOSITORY_PATH = "/tmp/enkive-indri";
	private static final String INDRI_TEMP_STORAGE_PATH = "/tmp/enkive-indri-tmp";

	static MongoGridDocStoreService docStoreService;
	static IndriDocSearchIndexService docIndexService;
	static IndriDocSearchQueryService docQueryService;
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
		final String identifier = storageResult.getIdentifier();

		if (!storageResult.getAlreadyStored()) {
			docIndexService.indexDocument(identifier);
		}
	}

	private static void archive(String content) throws DocStoreException,
			DocSearchException {
		StoreRequestResult result = docStoreService.store(new StringDocument(
				content, "text/plain", "txt", "7 bit"));
		indexSet.add(result.getIdentifier());

		System.out.println("archived string " + result.getIdentifier() + " "
				+ (result.getAlreadyStored() ? "OLD" : "NEW"));

		if (INDEXING_METHOD == Indexing.PUSH && !result.getAlreadyStored()) {
			index(result);
		}
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
						d.getDecodedContentStream(),
						Constants.PREFERRED_CHARSET));
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
					MimeUtil.ENC_QUOTED_PRINTABLE, "windows-1252"),
			new FileRecord(
					"4-b64.docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
					"docx", MimeUtil.ENC_BASE64) };

	private final static Set<String> encodedIdentifierSet = new HashSet<String>();

	private static void archiveEncoded() {
		for (FileRecord fileRec : encodedFiles) {
			try {
				FileSystemDocument d = new FileSystemDocument(inputDir + "/"
						+ fileRec.name, fileRec.mimeType, fileRec.suffix,
						fileRec.encoding);
				StoreRequestResult result = docStoreService.store(d);

				if (INDEXING_METHOD == Indexing.PUSH
						&& !result.getAlreadyStored()) {
					index(result);
				}

				final String identifier = result.getIdentifier();
				encodedIdentifierSet.add(identifier);
				System.out.println("archived encoded " + identifier + " "
						+ (result.getAlreadyStored() ? "OLD" : "NEW"));
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
				StreamConnector.transferForeground(d.getDecodedContentStream(),
						fileStream);
				fileStream.close();
				System.out.println("retrieving " + identifier + " to "
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
		List<String> theSearch = docQueryService.search(query);
		if (theSearch.isEmpty()) {
			System.out.println("no search results");
		} else {
			for (String docIdentifier : theSearch) {
				System.out.println(docIdentifier);
			}
		}
	}

	/**
	 * Pull an un-indexed document, mark as needing indexing, and then go ahead
	 * and index it
	 * 
	 * @return true if a document was pulled, false if no documents to pull
	 */
	private static boolean indexAnUnindexedDocument(int serverIndex,
			int serverCount) {
		String identifier = docStoreService.nextUnindexed(serverIndex,
				serverCount);
		if (identifier != null) {
			try {
				System.out.println("indexing: " + identifier + " (from server "
						+ serverIndex + ")");
				docIndexService.indexDocument(identifier);
				// docStoreService.markAsIndexed(identifier);
			} catch (Exception e) {
				System.err.println(e);
			}
			return true;
		} else {
			return false;
		}
	}

	private static boolean indexAnUnindexedDocument() {
		int serverCount = 5;
		for (int i = 0; i < serverCount; i++) {
			boolean result = indexAnUnindexedDocument(i, serverCount);
			if (result) {
				return result;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		try {
			if (DROP_DOCS_ON_STARTUP) {
				System.out.println("DROPPING existing documents");
				Dropper.dropDatabase(DATABASE_NAME);
			}
			docStoreService = new ConvenienceMongoGridDocStoreService(
					DATABASE_NAME, GRIDFS_COLLECTION_NAME);
			docStoreService.startup();

			docIndexService = new IndriDocSearchIndexService(docStoreService,
					new TikaContentAnalyzer(), INDRI_REPOSITORY_PATH,
					INDRI_TEMP_STORAGE_PATH);
			if (INDEXING_METHOD == Indexing.AUTO_PULL) {
				docIndexService.setIndexerQueueService(docStoreService.getIndexerQueueService());
				docIndexService
						.setUnindexedDocRePollInterval(INDEXING_POLL_TIME);
			}
			docIndexService.startup();

			docQueryService = new IndriDocSearchQueryService(
					INDRI_REPOSITORY_PATH);
			docQueryService.setQueryEnvironmentRefreshInterval(5);
			docQueryService.startup();

			archiveAll();
			retrieveAll();

			archiveEncoded();
			retrieveEncoded();

			if (INDEXING_METHOD == Indexing.MANUAL_PULL) {
				while (indexAnUnindexedDocument()) {
					// empty
				}
			} else {
				Thread.sleep(5000 * INDEXING_POLL_TIME);
			}

			searchFor("#1(the question)");
			docIndexService.refreshIndexEnvironment();
			docQueryService.refreshQueryEnvironment();
			searchFor("#1(the question)");

			searchFor("#1(BE THAT)");
			searchFor("#1(question the)");
			searchFor("test");
			searchFor("#2(civil test)");
			searchFor("#1(shall not perish)");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (docQueryService != null) {
					docQueryService.shutdown();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				if (docIndexService != null) {
					docIndexService.shutdown();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

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
