package com.linuxbox.enkive.docsearch.indri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;
import lemurproject.indri.ParsedDocument;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.AbstractDocSearchIndexService;
import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.StreamConnector;

public class IndriDocSearchIndexService extends AbstractDocSearchIndexService {
	/**
	 * This is supposed to be a call-back that gets status updates. But as of
	 * April 25, 2011, delete would be called for reasons unknown and using it
	 * in the call to IndexEnvironment::open resulted in runtime errors. So for
	 * now it's not used.
	 * 
	 * @author ivancich
	 * 
	 */
	static class RecordedIndexStatus extends IndexStatus {
		public void status(int code, String documentPath, String error,
				int documentsIndexed, int documentsSeen) {
			System.out.println("code: " + code);
			System.out.println("document path: " + documentPath);
			System.out.println("error: " + error);
			System.out.println("documents indexed: " + documentsIndexed);
			System.out.println("documents seen: " + documentsSeen);
		}

		public void delete() {
			System.err.println("IndexStatus::delete called\n");
		}
	}

	static enum IndexStorage {
		STRING, FILE, PARSED_DOCUMENT, BY_SIZE
	}

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	private static final boolean STORE_DOCUMENTS = false;
	private static final String NAME_FIELD = "docno";
	private static final String[] METADATA_FIELDS = { NAME_FIELD };

	private static final String STEMMER = "krovetz";

	/**
	 * How much memory (in bytes) to allow the INDRI indexer to use
	 */
	private static final long DEFAULT_MEMORY_TO_USE = 200 * 1024 * 1024; // 200MB

	/**
	 * How many documents can be indexed under a given IndexEnvironment before
	 * it's closed and re-created.
	 */
	private static final int DEFAULT_INDEX_ENV_DOC_LIMIT = 25;

	/**
	 * How many seconds between creating a new IndexEnvironment;
	 */
	private static final int DEFAULT_INDEX_ENV_REFRESH_INTERVAL = 60;
	private static final int DEFAULT_QUERY_ENV_REFRESH_INTERVAL = 300;

	private static final String TEXT_FORMAT = "txt";
	private static final String TRECTEXT_FORMAT = "trectext";
	private static final long DOC_SIZE_IN_MEMORY_LIMIT = 8 * 1024; // 8 KB
	// private static final IndexStorage INDEX_STORAGE = IndexStorage.BY_SIZE;
	// private static final IndexStorage INDEX_STORAGE =
	// IndexStorage.PARSED_DOCUMENT;
	private static final IndexStorage INDEX_STORAGE = IndexStorage.FILE;

	private String repositoryPath;
	private String tempStoragePath;
	private File tempStorageDir;

	/**
	 * How many documents to index before closing an index environment (which
	 * causes a save-to-disk) and opening a new one.
	 */
	private int indexEnvironmentDocLimit = DEFAULT_INDEX_ENV_DOC_LIMIT;

	/**
	 * How many seconds since an index environment was opened to close it (which
	 * causes a save-to-disk) and opening a new one.
	 */
	private int indexEnvironmentRefreshInterval = DEFAULT_INDEX_ENV_REFRESH_INTERVAL;

	/**
	 * How much memory to allow INDRI to use.
	 */
	private long indexEnvironmentMemory = DEFAULT_MEMORY_TO_USE;

	/**
	 * NOTE: TODO : since all action through the IndexEnvironment is serialized
	 * by getting actions off of a queue, this mutex is likely no longer
	 * necessary. Once code is stable and working, look into removing it.
	 * 
	 * Used to control access to the index environment; needed in case an
	 * outside caller wants to refresh the index environment.
	 */
	private Object indexEnvironmentMutex;

	private IndexEnvironment indexEnvironment;
	private long indexEnvironmentCreated;
	private int indexEnvironmentDocCount;

	/**
	 * We need a query environment for removal of documents from the index,
	 * since we're given the document name and must convert it to a document
	 * number.
	 */
	private QueryEnvironmentManager queryEnvironmentManager;

	/**
	 * Construct an Indri indexing service that does not do polling of the doc
	 * store.
	 * 
	 * @param docStoreService
	 * @param analyzer
	 * @param repositoryPath
	 * @param temporaryStoragePath
	 *            the path to a directory that indri can use for temporary
	 *            storage; the indexing service should clean up after itself
	 *            unless it crashes
	 * @throws DocSearchException
	 */
	public IndriDocSearchIndexService(DocStoreService docStoreService,
			ContentAnalyzer analyzer, String repositoryPath,
			String temporaryStoragePath) throws DocSearchException {
		super(docStoreService, analyzer);
		this.repositoryPath = repositoryPath;
		this.tempStoragePath = temporaryStoragePath;
		this.indexEnvironmentMutex = new Object();
	}

	/**
	 * Construct an Indri indexing service that will poll the doc store.
	 * 
	 * @param docStoreService
	 * @param analyzer
	 * @param repositoryPath
	 * @param temporaryStoragePath
	 *            the path to a directory that indri can use for temporary
	 *            storage; the indexing service should clean up after itself
	 *            unless it crashes
	 * @param unindexedDocSearchInterval
	 *            number of seconds to wait when the last poll resulted in no
	 *            unindexed documents
	 * @throws DocSearchException
	 */
	public IndriDocSearchIndexService(DocStoreService docStoreService,
			ContentAnalyzer analyzer, String repositoryPath,
			String temporaryStoragePath, int unindexedDocSearchInterval)
			throws DocSearchException {
		super(docStoreService, analyzer, unindexedDocSearchInterval);
		this.repositoryPath = repositoryPath;
		this.tempStoragePath = temporaryStoragePath;
		this.indexEnvironmentMutex = new Object();
		this.queryEnvironmentManager = new QueryEnvironmentManager(
				DEFAULT_QUERY_ENV_REFRESH_INTERVAL);
		this.queryEnvironmentManager.addIndexPath(repositoryPath);
	}

	@Override
	public void subStartup() throws DocSearchException {
		initializeTemporaryStorage(tempStoragePath);
		createIndexEnvironment();
	}

	@Override
	public void subShutdown() throws DocSearchException {
		LOGGER.trace("in IndriDocSearchIndexService::subShutdown");

		if (indexEnvironment != null) {
			try {
				synchronized (indexEnvironmentMutex) {
					indexEnvironment.close();
					indexEnvironment = null;
				}
			} catch (Exception e) {
				throw new DocSearchException(
						"could not shut down INDRI search service", e);
			}
		}
	}

	private static void createIndriRepository(
			IndexEnvironment indexEnvironment, String repositoryPath)
			throws DocSearchException {
		try {
			indexEnvironment.create(repositoryPath);
			indexEnvironment.close();
		} catch (Exception e) {
			throw new DocSearchException(
					"could not construct an INDRI repository", e);
		}
	}

	private void createIndexEnvironment() throws DocSearchException {
		indexEnvironment = new IndexEnvironment();

		try {
			indexEnvironment.setStoreDocs(STORE_DOCUMENTS);
			indexEnvironment.setStemmer(STEMMER);
			indexEnvironment.setIndexedFields(METADATA_FIELDS);
			indexEnvironment.setMetadataIndexedFields(METADATA_FIELDS,
					METADATA_FIELDS);
			indexEnvironment.setNormalization(true);

			if (indexEnvironmentMemory > 0) {
				indexEnvironment.setMemory(indexEnvironmentMemory);
			}
		} catch (Exception e) {
			throw new DocSearchException(
					"could not initialize INDRI index environment", e);
		}

		// try to open existing repository; if that fails try to create one and
		// open it
		try {
			openIndexEnvironment();
		} catch (Exception e1) {
			createIndriRepository(indexEnvironment, repositoryPath);
			try {
				openIndexEnvironment();
			} catch (Exception e2) {
				throw new DocSearchException(
						"could not open the INDRI repository", e2);
			}
		}
	}

	private void initializeTemporaryStorage(String temporaryStoragePath)
			throws DocSearchException {
		tempStorageDir = new File(temporaryStoragePath);

		if (!tempStorageDir.exists()) {
			if (!tempStorageDir.mkdirs()) {
				throw new DocSearchException(
						"could not find or create temporary storage directory \""
								+ temporaryStoragePath
								+ "\" for INDRI search service");
			}
		} else if (!tempStorageDir.isDirectory()) {
			throw new DocSearchException("temporary storage path \""
					+ temporaryStoragePath + "\" is not a directory");
		}

		if (!tempStorageDir.canWrite()) {
			throw new DocSearchException(
					"cannot write to temporary storage directory \""
							+ temporaryStoragePath + "\"");
		}

		if (!tempStorageDir.canExecute()) {
			throw new DocSearchException(
					"cannot access temporary storage directory \""
							+ temporaryStoragePath + "\"");
		}

		if (!tempStorageDir.canRead()) {
			throw new DocSearchException(
					"cannot read from temporary storage directory \""
							+ temporaryStoragePath + "\"");
		}
	}

	private int indexDocumentAsString(Document doc, String identifier)
			throws DocStoreException, DocSearchException {
		try {
			StringBuilder docString = new StringBuilder();
			Reader input = contentAnalyzer.parseIntoText(doc);
			char buffer[] = new char[4096];

			int charsRead;

			while ((charsRead = input.read(buffer)) > 0) {
				docString.append(buffer, 0, charsRead);
			}

			Map<String, String> metaData = new HashMap<String, String>();

			metaData.put(NAME_FIELD, identifier);

			int documentId;
			synchronized (indexEnvironmentMutex) {
				documentId = indexEnvironment.addString(docString.toString(),
						TEXT_FORMAT, metaData);
			}
			return documentId;
		} catch (DocStoreException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("could not index \"" + identifier
					+ "\"", e);
		}
	}

	private void indexDocumentAsFile(Document doc, String identifier)
			throws DocSearchException, DocStoreException {
		Reader input = null;
		try {
			input = contentAnalyzer.parseIntoText(doc);

			File tempFile = File.createTempFile("enkive-indri", ".txt",
					tempStorageDir);
			PrintWriter output = null;
			try {
				output = new PrintWriter(new FileWriter(tempFile));
				output.println("<DOC>");
				output.println("<" + NAME_FIELD + ">" + identifier + "</"
						+ NAME_FIELD + ">");
				output.println("<TEXT>");
				StreamConnector.transferForeground(input, output);
				output.println("</TEXT>");
				output.println("</DOC>");
				output.close();

				synchronized (indexEnvironmentMutex) {
					indexEnvironment.addFile(tempFile.getAbsolutePath(),
							TRECTEXT_FORMAT);
				}
			} finally {
				tempFile.delete();
			}
		} catch (IOException e) {
			throw new DocStoreException("could not index document \""
					+ identifier + "\"", e);
		} catch (Exception e) {
			throw new DocSearchException("could not index document \""
					+ identifier + "\"", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private int indexDocumentAsParsedDocument(Document doc, String identifier)
			throws DocSearchException, DocStoreException {
		ParsedDocument parsedDoc = new ParsedDocument();

		parsedDoc.metadata = new HashMap<Object, Object>();
		parsedDoc.metadata.put(NAME_FIELD, identifier);

		parsedDoc.terms = new String[] { "to", "be", "or", "not", "to", "be",
				"that", "is", "the", "question" };

		parsedDoc.content = "to be or not to be, that is the question";
		parsedDoc.text = "to be or not to be, that is the question";

		parsedDoc.content = "REDACTED";
		parsedDoc.text = "REDACTED";

		parsedDoc.positions = new ParsedDocument.TermExtent[] {
				new ParsedDocument.TermExtent(0, 2), // to
				new ParsedDocument.TermExtent(3, 5), // be
				new ParsedDocument.TermExtent(6, 8), // or
				new ParsedDocument.TermExtent(9, 12), // not
				new ParsedDocument.TermExtent(13, 15), // to
				new ParsedDocument.TermExtent(16, 18), // be
				new ParsedDocument.TermExtent(20, 24), // that
				new ParsedDocument.TermExtent(25, 27), // is
				new ParsedDocument.TermExtent(28, 31), // the
				new ParsedDocument.TermExtent(32, 40), // question
		};

		try {
			// do not use indexEnvironmentMutex because this is testing code
			final int docId = indexEnvironment.addParsedDocument(parsedDoc);
			return docId;
		} catch (Exception e) {
			throw new DocSearchException("could not indexed ParsedDocument \""
					+ identifier + "\"");
		}
	}

	@SuppressWarnings("unchecked")
	private int indexDocumentAsParsedDocument2(Document doc, String identifier)
			throws DocSearchException, DocStoreException {
		ParsedDocument parsedDoc = new ParsedDocument();

		parsedDoc.metadata = new HashMap<Object, Object>();
		parsedDoc.metadata.put(NAME_FIELD, identifier);

		parsedDoc.terms = new String[] { "TO", "BE", "OR", "NOT", "TO", "BE",
				"THAT", "IS", "THE", "QUESTION" };

		parsedDoc.content = "to be or not to be, that is the question";
		parsedDoc.text = "to be or not to be, that is the question";

		parsedDoc.content = "REDACTED";
		parsedDoc.text = "REDACTED";

		parsedDoc.positions = new ParsedDocument.TermExtent[] {
				new ParsedDocument.TermExtent(0, 2), // to
				new ParsedDocument.TermExtent(3, 5), // be
				new ParsedDocument.TermExtent(6, 8), // or
				new ParsedDocument.TermExtent(9, 12), // not
				new ParsedDocument.TermExtent(13, 15), // to
				new ParsedDocument.TermExtent(16, 18), // be
				new ParsedDocument.TermExtent(20, 24), // that
				new ParsedDocument.TermExtent(25, 27), // is
				new ParsedDocument.TermExtent(28, 31), // the
				new ParsedDocument.TermExtent(32, 40), // question
		};

		try {
			// do not use indexEnvironmentMutex because this is testing code
			final int docId = indexEnvironment.addParsedDocument(parsedDoc);
			return docId;
		} catch (Exception e) {
			throw new DocSearchException("could not indexed ParsedDocument "
					+ identifier);
		}
	}

	@Override
	protected void doIndexDocument(String identifier)
			throws DocSearchException, DocStoreException {
		LOGGER.trace("starting to index " + identifier);

		@SuppressWarnings("unused")
		int docId = -1;
		try {
			Document doc = docStoreService.retrieve(identifier);
			switch (INDEX_STORAGE) {
			case STRING:
				docId = indexDocumentAsString(doc, identifier);
				break;
			case FILE:
				indexDocumentAsFile(doc, identifier);
				break;
			case PARSED_DOCUMENT:
				docId = indexDocumentAsParsedDocument2(doc, identifier);
				break;
			default:
				LOGGER.warn("IndriSearchService::INDEX_STORAGE storage strategy has unexpected "
						+ "value; using size as determiner");
				// falling through
			case BY_SIZE:
				if (doc.getEncodedSize() > DOC_SIZE_IN_MEMORY_LIMIT) {
					indexDocumentAsFile(doc, identifier);
				} else {
					docId = indexDocumentAsString(doc, identifier);
				}
				break;
			}

			// this will close the index environment and re-open it under
			// certain conditions
			indexPostProcessing();

			LOGGER.info("indexed " + identifier);
		} catch (DocStoreException e) {
			throw e;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("errors managing INDRI index", e);
		}
	}

	@Override
	protected void doRemoveDocument(String identifier)
			throws DocSearchException {
		try {
			LOGGER.trace("starting to remove " + identifier);
			int[] documentNumbers = lookupDocumentNumbers(identifier);

			if (documentNumbers.length > 1) {
				LOGGER.warn("removing multiple (" + documentNumbers.length
						+ ") documents in the index with name \"" + identifier
						+ "\"");
			}

			// Save the last exception of possibly many to throw when finished
			// with best effort.
			Exception savedException = null;

			synchronized (indexEnvironmentMutex) {
				for (int documentNumber : documentNumbers) {
					try {
						indexEnvironment.deleteDocument(documentNumber);
					} catch (Exception e) {
						savedException = e;
					}
				}
			}

			if (savedException != null) {
				throw savedException;
			}

			LOGGER.info("removed " + identifier);
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("could not remove document \""
					+ identifier + "\" from index", e);
		}
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	/**
	 * Number of seconds between QueryEnvironment refreshes.
	 * 
	 * @return
	 */
	public int getQueryEnvironmentRefreshInterval() {
		return (int) (this.queryEnvironmentManager
				.getQueryEnvironmentRefreshInterval() / 1000);
	}

	/**
	 * Number of seconds between QueryEnvironment refreshes.
	 * 
	 * @return
	 */
	public void setQueryEnvironmentRefreshInterval(int refreshIntervalSeconds) {
		this.queryEnvironmentManager
				.setQueryEnvironmentRefreshInterval(refreshIntervalSeconds * 1000);
	}

	public int getIndexEnvironmentDocLimit() {
		return indexEnvironmentDocLimit;
	}

	public void setIndexEnvironmentDocLimit(int indexEnvironmentDocLimit) {
		this.indexEnvironmentDocLimit = indexEnvironmentDocLimit;
	}

	public int getIndexEnvironmentRefreshInterval() {
		return indexEnvironmentRefreshInterval;
	}

	public void setIndexEnvironmentRefreshInterval(int refreshIntervalSeconds) {
		this.indexEnvironmentRefreshInterval = refreshIntervalSeconds;
	}

	public long getIndexEnvironmentMemory() {
		return indexEnvironmentMemory;
	}

	public void setIndexEnvironmentMemory(long indexEnvironmentMemory) {
		this.indexEnvironmentMemory = indexEnvironmentMemory;
	}

	private void openIndexEnvironment() throws Exception {
		if (indexEnvironmentMemory > 0) {
			// in case it was changed, we can modify the amount at
			// re-open...MAYBE depending on how INDRI handles this.
			indexEnvironment.setMemory(indexEnvironmentMemory);
		}
		indexEnvironment.open(repositoryPath);
		indexEnvironmentDocCount = 0;
		indexEnvironmentCreated = System.currentTimeMillis();
	}

	public void refreshIndexEnvironment() throws DocSearchException {
		try {
			synchronized (indexEnvironmentMutex) {
				indexEnvironment.close();
				openIndexEnvironment();
			}
		} catch (Exception e) {
			throw new DocSearchException("could not refresh index environment",
					e);
		}
	}

	private void indexPostProcessing() throws DocSearchException {
		indexEnvironmentDocCount++;
		final long now = System.currentTimeMillis();
		final long timeSpanMilliseconds = now - indexEnvironmentCreated;
		if (timeSpanMilliseconds > 1000 * indexEnvironmentRefreshInterval
				|| indexEnvironmentDocCount >= indexEnvironmentDocLimit) {
			try {
				refreshIndexEnvironment();
			} catch (Exception e) {
				throw new DocSearchException(
						"could not close/open index environment", e);
			}
		}
	}

	/**
	 * Given a document identifier this will return the document number(s) with
	 * a matching identifier; in most cases this should be one or zero, but it
	 * can handle multiple matches as well.
	 * 
	 * @param identifier
	 * @return
	 * @throws DocSearchException
	 */
	private int[] lookupDocumentNumbers(String identifier)
			throws DocSearchException {
		int[] documentNumbers;
		final String[] idListOfOne = { identifier };

		// try to find the document up to two times; if the first attempt
		// returns 0 documents or generates an exception, refresh the
		// QueryEnvironment and try again; this will handle
		// cases in which the document was indexed since the last refresh.
		for (int i = 1; i <= 2; i++) {
			try {
				if (i == 2) {
					refreshIndexEnvironment();
					queryEnvironmentManager.forceQueryEnvironmentRefresh();
				}
				documentNumbers = queryEnvironmentManager.getQueryEnvironment()
						.documentIDsFromMetadata(NAME_FIELD, idListOfOne);
				if (documentNumbers.length > 0) {
					return documentNumbers;
				}
			} catch (Exception e) {
				if (i == 2) {
					throw new DocSearchException(
							"could not query index for document \""
									+ identifier + "\"", e);
				}
			}
		}

		throw new DocSearchException("could not find document \"" + identifier
				+ "\" for removal");
	}
}
