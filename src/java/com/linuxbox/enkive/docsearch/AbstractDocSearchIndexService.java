package com.linuxbox.enkive.docsearch;

import static com.linuxbox.enkive.docstore.DocStoreConstants.QUEUE_ENTRY_INDEX_DOCUMENT;
import static com.linuxbox.enkive.docstore.DocStoreConstants.QUEUE_ENTRY_REMOVE_DOCUMENT;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.DocStoreConstants;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.InterruptableSleeper;
import com.linuxbox.util.queueservice.QueueEntry;
import com.linuxbox.util.queueservice.QueueService;
import com.linuxbox.util.queueservice.QueueServiceException;

public abstract class AbstractDocSearchIndexService implements
		DocSearchIndexService {
	static enum Status {
		BEFORE_STARTED, RUNNING, STOPPING, STOPPED
	};

	/**
	 * This is a thread that will try to pull documents from the document store
	 * that are un-indexed and index them. If it finds there are currently no
	 * un-indexed documents, it will go to sleep for a little and then try
	 * again.
	 * 
	 * @author ivancich
	 * 
	 */
	class IndexPullingThread extends Thread {
		InterruptableSleeper sleeper;
		Status status;

		public IndexPullingThread() {
			status = Status.BEFORE_STARTED;

			// so it's never null
			sleeper = new InterruptableSleeper();
		}

		void markAsErrorIndexing(String documentId, Throwable exception) {
			try {
				LOGGER.error("Unable to index document: " + documentId,
						exception);
				docStoreService.markAsErrorIndexing(documentId);
			} catch (DocStoreException e) {
				LOGGER.error(
						"Unable to mark document as having indexing error: "
								+ documentId, e);
			}
		}

		public void stopAfterFinishingUp(long maximumToWait) {
			status = Status.STOPPING;
			sleeper.interrupt();
			try {
				LOGGER.trace("waiting for indexing thread to finish up");
				this.join(maximumToWait);
				LOGGER.trace("indexing thread finished up");
			} catch (InterruptedException e) {
				LOGGER.warn("IndexPullingThread interrupted", e);
			}
		}

		public void run() {
			status = Status.RUNNING;
			sleeper.start();

			while (status == Status.RUNNING) {
				String documentId;
				while (status == Status.RUNNING) {
					boolean error = false;
					QueueEntry entry;
					try {
						entry = indexerQueueService.dequeue();
						if (entry == null) {
							break;
						}
					} catch (QueueServiceException e) {
						LOGGER.error("could not access indexer queue", e);
						break;
					}

					final long startTime = System.currentTimeMillis();

					documentId = entry.getIdentifier();

					Integer note;
					if (entry.getNote() instanceof Integer) {
						note = (Integer) entry.getNote();
					} else {
						note = Integer.MIN_VALUE; // force error below
					}

					switch (note) {
					case QUEUE_ENTRY_INDEX_DOCUMENT:
						try {
							doIndexDocument(documentId);
						} catch (Exception e) {
							LOGGER.warn("got exception while indexing", e);
							markAsErrorIndexing(documentId, e);
							error = true;
						}
						break;
					case QUEUE_ENTRY_REMOVE_DOCUMENT:
						try {
							doRemoveDocument(documentId);
						} catch (Exception e) {
							LOGGER.error(
									"got exception while trying to remove \""
											+ documentId + "\"", e);
							error = true;
						}
						break;
					default:
						LOGGER.error("could not interpret note of \""
								+ entry.getNote() + "\"");
					}

					try {
						if (error) {
							indexerQueueService.markEntryAsError(entry);
						} else {
							indexerQueueService.finishEntry(entry);
						}
					} catch (QueueServiceException e) {
						LOGGER.error("could note finalize indexer queue entry (\""
								+ documentId + "\" / " + note + ")");
					} finally {
						if (LOGGER.isTraceEnabled()) {
							final long endTime = System.currentTimeMillis();
							LOGGER.trace("TIMING: "
									+ (endTime - startTime)
									+ " ms to "
									+ (note == QUEUE_ENTRY_INDEX_DOCUMENT ? "index "
											: "de-index ") + documentId
									+ (error ? " w/ ERROR" : ""));
						}
					}
				} // inner while loop

				sleeper.waitFor(unindexedDocRePollInterval);
				if (sleeper.wasInterrupted()) {
					break;
				}
			} // outer while loop

			status = Status.STOPPED;
		}
	}

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch");

	private static final long MAX_SHUTTING_DOWN_WAIT = 30000;

	/**
	 * The document storage service we're feeding off of.
	 */
	protected DocStoreService docStoreService;

	protected ContentAnalyzer contentAnalyzer;

	private QueueService indexerQueueService;

	/**
	 * In MILLISECONDS, although the API exposes it as SECONDS for convenience
	 * from the outside; non-positive values indicate that there is no automated
	 * query for un-indexed documents
	 */
	private int unindexedDocRePollInterval;

	/**
	 * True if trying to shut down; this flag will prevent a new thread from
	 * being created while we're trying to shut down the current one.
	 */
	protected boolean shuttingDown;

	/**
	 * The thread that polls the DocStoreService for unindexed documents.
	 */
	private IndexPullingThread indexPullingThread;

	public AbstractDocSearchIndexService(DocStoreService service,
			ContentAnalyzer analyzer) {
		setDocStoreService(service);
		setContentAnalyzer(analyzer);
		this.shuttingDown = false;
		this.unindexedDocRePollInterval = -1;
	}

	public AbstractDocSearchIndexService(DocStoreService service,
			ContentAnalyzer analyzer, int unindexedDocSearchInterval) {
		this(service, analyzer);
		this.unindexedDocRePollInterval = unindexedDocSearchInterval;
	}

	/*
	 * LIFECYCLE METHODS
	 * 
	 * We'll take control of the start-up and shut-down process and delegate to
	 * our subclasses. Otherwise we'll have to rely on the subclasses chaining
	 * up to us. Subclasses will have to implement subStartup and subShutdown,
	 * which we will call. startup and shutdown are "final" so they cannot be
	 * overridden in subclasses.
	 */

	protected abstract void subStartup() throws DocSearchException;

	protected abstract void subShutdown() throws DocSearchException;

	@Override
	public final void startup() throws DocSearchException {
		// first I start up if there's anything I need to do for them
		if (indexerQueueService == null) {
			throw new DocSearchException("no indexer queue service was set");
		}

		// then they start up
		subStartup();

		// start the thread after they're up and running since we're calling
		// down into them
		managePullThread(unindexedDocRePollInterval);
	}

	@Override
	public final void shutdown() throws DocSearchException {
		LOGGER.trace("starting shutdown of DocSearchIndexService");

		// first I shut down my index pulling thread if I have one
		synchronized (this) {
			shuttingDown = true;
			if (indexPullingThread != null) {
				indexPullingThread.stopAfterFinishingUp(MAX_SHUTTING_DOWN_WAIT);
			}
		}

		// now that I won't be asking to index or remove documents, the
		// implementation class can now be shut down
		subShutdown();

		// then I shut down anything else
		LOGGER.trace("finished shutdown of DocSearchIndexService");
	}

	@Override
	public final void indexDocument(String identifier)
			throws DocSearchException {
		try {
			indexerQueueService.enqueue(identifier, AbstractDocStoreService
					.getShardIndexFromIdentifier(identifier),
					DocStoreConstants.QUEUE_ENTRY_INDEX_DOCUMENT);
		} catch (QueueServiceException e) {
			throw new DocSearchException("could not add indexing of \""
					+ identifier + "\" to indexing queue");
		}
	}

	@Override
	public void indexDocuments(Collection<String> identifiers)
			throws DocStoreException, DocSearchException {
		for (String identifier : identifiers) {
			indexDocument(identifier);
		}
	}

	@Override
	public void removeDocument(String identifier) throws DocSearchException {
		try {
			indexerQueueService.enqueue(identifier, AbstractDocStoreService
					.getShardIndexFromIdentifier(identifier),
					DocStoreConstants.QUEUE_ENTRY_REMOVE_DOCUMENT);
		} catch (QueueServiceException e) {
			throw new DocSearchException("could not add removal of \""
					+ identifier + "\" to indexing queue");
		}
	}

	@Override
	public void setDocStoreService(DocStoreService service) {
		this.docStoreService = service;
	}

	@Override
	public void setContentAnalyzer(ContentAnalyzer analyzer) {
		this.contentAnalyzer = analyzer;
	}

	@Override
	public void setUnindexedDocRePollInterval(int seconds) {
		unindexedDocRePollInterval = seconds * 1000;
		managePullThread(unindexedDocRePollInterval);
	}

	public void setIndexerQueueService(QueueService indexerQueueService) {
		this.indexerQueueService = indexerQueueService;
	}

	private synchronized void managePullThread(int milliseconds) {
		if (!shuttingDown) {
			if (milliseconds > 0 && indexPullingThread == null) {
				indexPullingThread = new IndexPullingThread();
				indexPullingThread.start();
			} else if (milliseconds <= 0 && indexPullingThread != null) {
				indexPullingThread.stopAfterFinishingUp(MAX_SHUTTING_DOWN_WAIT);
				indexPullingThread = null;
			}
		}
	}

	/**
	 * Actually index the document described the given identifier.
	 * 
	 * @param identifier
	 *            the unique identifier associated with the document
	 * @throws DocStoreException
	 * @throws DocSearchException
	 */
	protected abstract void doIndexDocument(String identifier)
			throws DocStoreException, DocSearchException;

	protected abstract void doRemoveDocument(String id)
			throws DocSearchException;
}
