/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.docsearch.indri;

import static com.linuxbox.enkive.docsearch.indri.IndriDocSearchIndexService.IndexStorage.FILE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;
import lemurproject.indri.ParsedDocument;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.AbstractDocSearchIndexService;
import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreConstants;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.util.DirectoryManagement;
import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockReleaseException;
import com.linuxbox.util.lockservice.LockService;

public class IndriDocSearchIndexService extends AbstractDocSearchIndexService {
	static enum IndexStorage {
		STRING, FILE, PARSED_DOCUMENT, BY_SIZE
	}

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	/**
	 * Determines whether temporary files created so Indri can index them are
	 * removed after either Indri is done with them or if there were a problem
	 * during indexing (perhaps by the text analyzer).
	 */
	private final static boolean REMOVE_TEMP_FILES = true;

	/*
	 * These are used to obtain the locks.
	 */
	private final static int LOCK_RETRIES = 10;
	private final static long LOCK_RETRY_DELAY_MILLISECONDS = 10000;

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
	 * it's closed and re-created. Set to non-positive value if no limit should
	 * be enforced.
	 */
	private static final int DEFAULT_INDEX_ENV_DOC_LIMIT = 0;

	/**
	 * How many seconds between creating a new IndexEnvironment
	 */
	private static final int DEFAULT_INDEX_ENV_REFRESH_INTERVAL = 5 * 60; // 5
																			// minutes

	private static final int DEFAULT_QUERY_ENV_REFRESH_INTERVAL = 5 * 60; // 5
																			// minutes
	private static final String TEXT_FORMAT = "txt";
	private static final String TRECTEXT_FORMAT = "trectext";
	private static final long DOC_SIZE_IN_MEMORY_LIMIT = 8 * 1024; // 8 KB
	private static final IndexStorage INDEX_STORAGE = FILE;

	private final String repositoryPath;
	private final File tempStorageDir;
	private final IndexServiceIndexStatus indexStatus;
	private final IndriIndexEnvironmentManager indexEnvironmentManager;

	/**
	 * How many documents to index before closing an index environment (which
	 * causes a save-to-disk) and opening a new one. Should be non-positive if
	 * no limit should be enforced.
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

	// FIXME remove these
	// private Object indexEnvironmentMutex;
	// private IndexEnvironment indexEnvironment;

	/**
	 * We need a query environment for removal of documents from the index,
	 * since we're given the document name and must convert it to a document
	 * number.
	 */
	private QueryEnvironmentManager queryEnvironmentManager;

	private LockService documentLockingService;

	/**
	 * Construct an Indri indexing service that does not do polling of the doc
	 * store.
	 * 
	 * @param docStoreService
	 * @param analyzer
	 * @param repositoryPath
	 * @param tempStoragePath
	 *            the path to a directory that indri can use for temporary
	 *            storage; the indexing service should clean up after itself
	 *            unless it crashes
	 * @throws DocSearchException
	 */
	public IndriDocSearchIndexService(DocStoreService docStoreService,
			ContentAnalyzer analyzer, String repositoryPath,
			String tempStoragePath) throws DocSearchException {
		super(docStoreService, analyzer);
		verifyDirectories(repositoryPath, tempStoragePath);

		this.repositoryPath = repositoryPath;
		this.tempStorageDir = new File(tempStoragePath);
		this.indexStatus = new IndexServiceIndexStatus();
		this.indexEnvironmentManager = new IndriIndexEnvironmentManager();
	}

	/**
	 * Construct an Indri indexing service that will poll the doc store.
	 * 
	 * @param docStoreService
	 * @param analyzer
	 * @param repositoryPath
	 * @param tempStoragePath
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
			String tempStoragePath, int unindexedDocSearchInterval)
			throws DocSearchException {
		super(docStoreService, analyzer, unindexedDocSearchInterval);
		verifyDirectories(repositoryPath, tempStoragePath);

		this.repositoryPath = repositoryPath;
		this.tempStorageDir = new File(tempStoragePath);
		this.queryEnvironmentManager = new QueryEnvironmentManager(
				DEFAULT_QUERY_ENV_REFRESH_INTERVAL);
		this.queryEnvironmentManager.addIndexPath(repositoryPath);
		this.indexStatus = new IndexServiceIndexStatus();
		this.indexEnvironmentManager = new IndriIndexEnvironmentManager();
	}

	private void verifyDirectories(String repositoryPath, String tempStoragePath)
			throws DocSearchException {
		try {
			DirectoryManagement.verifyDirectory(tempStoragePath,
					"INDRI indexing temporary storage directory");
			DirectoryManagement.verifyDirectory(repositoryPath,
					"INDRI index directory");
		} catch (IOException e) {
			throw new DocSearchException(e);
		}
	}

	@Override
	public void subStartup() throws DocSearchException {
		if (documentLockingService == null) {
			throw new DocSearchException(
					"no document locking service was set for the IndroDocSearchIndexService");
		}
	}

	@Override
	public void subShutdown() throws DocSearchException {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("in IndriDocSearchIndexService::subShutdown");
		}
		indexEnvironmentManager.shutdown();
	}

	private int indexDocumentAsString(Document doc, String identifier)
			throws DocStoreException, DocSearchException {
		try {
			final StringBuilder docString = new StringBuilder();
			final Reader input = contentAnalyzer.parseIntoText(doc);
			final char buffer[] = new char[4096];

			int charsRead;

			while ((charsRead = input.read(buffer)) > 0) {
				docString.append(buffer, 0, charsRead);
			}

			final Map<String, String> metaData = new HashMap<String, String>();

			metaData.put(NAME_FIELD, identifier);

			int documentId = indexEnvironmentManager
					.doAction(new IndexEnvironmentAction<Integer, Exception>() {
						@Override
						public Integer withIndexEnvironmentDo(
								IndexEnvironment indexEnvironment)
								throws Exception {
							return indexEnvironment.addString(
									docString.toString(), TEXT_FORMAT, metaData);
						}
					});

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
		PrintWriter output = null;
		File tempFile = null;

		// assume one is thrown until we know none was (i.e., at end of try
		// block)
		boolean exceptionThrown = true;

		try {
			tempFile = File.createTempFile("enkive-indri-", ".trectext",
					tempStorageDir);

			input = contentAnalyzer.parseIntoText(doc);
			output = new PrintWriter(new BufferedWriter(
					new FileWriter(tempFile)));
			output.println("<DOC>");
			output.println("<" + NAME_FIELD + ">" + identifier + "</"
					+ NAME_FIELD + ">");
			output.println("<TEXT>");
			IOUtils.copy(input, output);
			output.println("</TEXT>");
			output.println("</DOC>");
			IOUtils.closeQuietly(output);
			IOUtils.closeQuietly(input);

			final String tempFileAbsolutePath = tempFile.getAbsolutePath();
			indexEnvironmentManager
					.doAction(new IndexEnvironmentAction<Object, Exception>() {
						@Override
						public Object withIndexEnvironmentDo(
								IndexEnvironment indexEnvironment)
								throws Exception {
							indexEnvironment.addFile(tempFileAbsolutePath,
									TRECTEXT_FORMAT);
							return null; // we don't need to return anything
											// since addFile doesn't
						}
					});

			exceptionThrown = false;
		} catch (IOException e) {
			throw new DocStoreException(
					"could not generate file to index document \"" + identifier
							+ "\"", e);
		} catch (Exception e) {
			throw new DocSearchException("could not index document \""
					+ identifier + "\"", e);
		} finally {
			if (output != null) {
				IOUtils.closeQuietly(output);
			}
			if (input != null) {
				IOUtils.closeQuietly(input);
			}
			if (exceptionThrown && REMOVE_TEMP_FILES && null != tempFile) {
				tempFile.delete();
			}
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private int indexDocumentAsParsedDocument(Document doc, String identifier)
			throws DocSearchException, DocStoreException {
		final ParsedDocument parsedDoc = new ParsedDocument();

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
			final int docId = indexEnvironmentManager
					.doAction(new IndexEnvironmentAction<Integer, Exception>() {
						@Override
						public Integer withIndexEnvironmentDo(
								IndexEnvironment indexEnvironment)
								throws Exception {
							return indexEnvironment
									.addParsedDocument(parsedDoc);
						}
					});

			return docId;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("could not indexed ParsedDocument \""
					+ identifier + "\"");
		}
	}

	@SuppressWarnings("unchecked")
	private int indexDocumentAsParsedDocument2(Document doc, String identifier)
			throws DocSearchException, DocStoreException {
		final ParsedDocument parsedDoc = new ParsedDocument();

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
			final int docId = indexEnvironmentManager
					.doAction(new IndexEnvironmentAction<Integer, Exception>() {
						@Override
						public Integer withIndexEnvironmentDo(
								IndexEnvironment indexEnvironment)
								throws Exception {
							return indexEnvironment
									.addParsedDocument(parsedDoc);
						}
					});

			return docId;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("could not indexed ParsedDocument "
					+ identifier);
		}
	}

	@Override
	protected void doIndexDocument(String identifier)
			throws DocSearchException, DocStoreException {
		if (LOGGER.isTraceEnabled())
			LOGGER.trace("starting to index " + identifier);

		@SuppressWarnings("unused")
		int docId;

		try {
			documentLockingService.lockWithRetries(identifier,
					DocStoreConstants.LOCK_TO_INDEX, LOCK_RETRIES,
					LOCK_RETRY_DELAY_MILLISECONDS);
		} catch (LockAcquisitionException e) {
			throw new DocStoreException(
					"could not acquire lock to index document \"" + identifier
							+ "\"", e);
		}
		try {
			Document doc = docStoreService.retrieve(identifier);
			switch (INDEX_STORAGE) {
			case STRING:
				docId = indexDocumentAsString(doc, identifier);
				break;
			case PARSED_DOCUMENT:
				docId = indexDocumentAsParsedDocument2(doc, identifier);
				break;
			case BY_SIZE:
				if (doc.getEncodedSize() > DOC_SIZE_IN_MEMORY_LIMIT) {
					indexDocumentAsFile(doc, identifier);
				} else {
					docId = indexDocumentAsString(doc, identifier);
				}
				break;

			// handle FILE and anything else
			default:
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("IndriSearchService::INDEX_STORAGE storage strategy has unexpected "
							+ "value; using size as determiner");
				// falling through
			case FILE:
				indexDocumentAsFile(doc, identifier);
				break;
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("indexed " + identifier);
			}
		} catch (DocumentNotFoundException e) {
			if (LOGGER.isWarnEnabled())
				LOGGER.warn("Document " + identifier + " not found: " + e);
		} catch (DocStoreException e) {
			throw e;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("errors managing INDRI index", e);
		} finally {
			try {
				documentLockingService.releaseLock(identifier);
			} catch (LockReleaseException e) {
				throw new DocStoreException(
						"could not release lock for document \"" + identifier
								+ "\"");
			}
		}
	}

	@Override
	protected void doRemoveDocument(String identifier)
			throws DocSearchException {
		try {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("starting to remove " + identifier);
			}

			final int[] documentNumbers = lookupDocumentNumbers(identifier);

			if (documentNumbers.length > 1) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("removing multiple (" + documentNumbers.length
							+ ") documents in the index with name \""
							+ identifier + "\"");
			}

			// Save the last exception of possibly many to throw when finished
			// with best effort.
			Exception savedException = indexEnvironmentManager
					.doAction(new IndexEnvironmentAction<Exception, Exception>() {
						@Override
						public Exception withIndexEnvironmentDo(
								IndexEnvironment indexEnvironment)
								throws Exception {
							Exception keepLastException = null;
							for (int documentNumber : documentNumbers) {
								try {
									indexEnvironment
											.deleteDocument(documentNumber);
								} catch (Exception e) {
									keepLastException = e;
								}
							}
							return keepLastException;
						}
					});

			if (savedException != null) {
				throw savedException;
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("removed " + identifier);
			}
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException(
					"could not remove one or more documents \"" + identifier
							+ "\" from index", e);
		}
	}

	public void refreshIndexEnvironment() throws DocSearchException {
		indexEnvironmentManager.forceRefresh();
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

	public LockService getDocumentLockingService() {
		return documentLockingService;
	}

	public void setDocumentLockingService(LockService documentLockingService) {
		this.documentLockingService = documentLockingService;
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
					indexEnvironmentManager.forceRefresh();
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

	static class IndexServiceIndexStatus extends IndexStatus {
		@Override
		public void status(int intCode, String path, String error,
				int docsIndexed, int docsSeen) {
			IndexStatus.action_code code = IndexStatus.action_code
					.swigToEnum(intCode);
			if (code == IndexStatus.action_code.FileClose) {
				if (REMOVE_TEMP_FILES) {
					final File f = new File(path);
					if (!f.delete()) {
						LOGGER.error("could not delete temporary INDRI indexing file "
								+ path);
					}
				}
			} else if (code == IndexStatus.action_code.FileSkip) {
				LOGGER.info("skipping file " + path);
			} else if (code == IndexStatus.action_code.FileError) {
				LOGGER.error("got error " + error + " with file " + path);
			}
		}
	}

	static interface IndexEnvironmentAction<ReturnType, ExceptionType extends Throwable> {
		abstract ReturnType withIndexEnvironmentDo(
				IndexEnvironment indexEnvironment) throws ExceptionType;
	}

	class IndriIndexEnvironmentManager {
		private boolean shutdown = false;
		private IndexEnvironment indexEnvironment;
		private boolean indexEnvironmentIsOpen = false;
		private int actionsAttempted = 0;
		private Timer timer = new Timer();
		private TimerTask indexEnvRefreshTask = null;

		public IndriIndexEnvironmentManager() throws DocSearchException {
			createIndexEnvironment();
		}

		public void shutdown() throws DocSearchException {
			shutdown = true;
			closeIndexEnvironment();
			timer.cancel();
		}

		public synchronized <ReturnType, ExceptionType extends Throwable> ReturnType doAction(
				IndexEnvironmentAction<ReturnType, ExceptionType> action)
				throws DocSearchException, ExceptionType {
			final IndexEnvironment env = getIndexEnvironment();
			try {
				final ReturnType result = action.withIndexEnvironmentDo(env);
				return result;
			} finally {
				++actionsAttempted;
				if (indexEnvironmentDocLimit > 0
						&& actionsAttempted >= indexEnvironmentDocLimit) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Index environment document action limit reached triggering index environment refresh.");
					}
					closeIndexEnvironment();
				}
			}
		}

		public synchronized void forceRefresh() throws DocSearchException {
			closeIndexEnvironment();
		}

		private synchronized IndexEnvironment getIndexEnvironment()
				throws DocSearchException {
			if (indexEnvironmentIsOpen) {
				return indexEnvironment;
			}

			openIndexEnvironment();

			return indexEnvironment;
		}

		private synchronized void openIndexEnvironment()
				throws DocSearchException {
			if (shutdown) {
				throw new DocSearchException(
						"tried to open an IndexEnivronment after service was shut down");
			} else if (indexEnvironmentIsOpen) {
				return;
			}

			try {
				if (indexEnvironmentMemory > 0) {
					// in case it was changed, we can modify the amount at
					// re-open...MAYBE depending on how INDRI handles this.
					indexEnvironment.setMemory(indexEnvironmentMemory);
				}
				indexEnvironment.open(repositoryPath, indexStatus);
				indexEnvironmentIsOpen = true;
				actionsAttempted = 0;
				if (indexEnvRefreshTask != null) {
					indexEnvRefreshTask.cancel();
				}
				indexEnvRefreshTask = new RefreshIndexEnvironmentTimerTask();
				timer.schedule(indexEnvRefreshTask,
						1000 * indexEnvironmentRefreshInterval);

				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("index environment opened");
				}
			} catch (Exception e) {
				throw new DocSearchException(
						"could not open an IndexEnvironment", e);
			}
		}

		private synchronized void closeIndexEnvironment()
				throws DocSearchException {
			if (!indexEnvironmentIsOpen) {
				return;
			}

			if (null != indexEnvRefreshTask) {
				indexEnvRefreshTask.cancel();
				indexEnvRefreshTask = null;
			}

			try {
				indexEnvironment.close();
				LOGGER.trace("index environment closed");
			} catch (Exception e) {
				throw new DocSearchException(
						"could not close IndexEnvironment", e);
			} finally {
				indexEnvironmentIsOpen = false;
			}
		}

		// protected void refreshIndexEnvironment() throws DocSearchException {
		// try {
		// synchronized (indexEnvironmentMutex) {
		// indexEnvironment.close();
		// openIndexEnvironment();
		// }
		// } catch (Exception e) {
		// throw new DocSearchException(
		// "could not refresh index environment", e);
		// }
		// }

		// private void indexPostProcessing() throws DocSearchException {
		// indexEnvironmentDocCount++;
		// final long now = System.currentTimeMillis();
		// final long timeSpanMilliseconds = now - indexEnvironmentCreated;
		// if (timeSpanMilliseconds > 1000 * indexEnvironmentRefreshInterval
		// || indexEnvironmentDocCount >= indexEnvironmentDocLimit) {
		// try {
		// refreshIndexEnvironment();
		// } catch (Exception e) {
		// throw new DocSearchException(
		// "could not close/open index environment", e);
		// }
		// }
		// }

		private synchronized void createIndexEnvironment()
				throws DocSearchException {
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

			// try to open existing repository; if that fails try to create one
			// and open it
			try {
				openIndexEnvironment();
				closeIndexEnvironment();
			} catch (Exception e1) {
				createIndriRepository(indexEnvironment, repositoryPath);
				try {
					openIndexEnvironment();
					closeIndexEnvironment();
				} catch (Exception e2) {
					throw new DocSearchException(
							"could not open the INDRI repository", e2);
				}
			}
		}

		private synchronized void createIndriRepository(
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

		class RefreshIndexEnvironmentTimerTask extends TimerTask {
			@Override
			public void run() {
				try {
					LOGGER.trace("index environment refresh timer triggered");
					closeIndexEnvironment();
				} catch (DocSearchException e) {
					LOGGER.error(
							"Timer-based index environment refresh got error.",
							e);
				}
			}
		}
	} // class IndexEnvironmentManager
} // class IndriDocSearchIndexService
