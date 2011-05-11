package com.linuxbox.enkive.docsearch;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public abstract class AbstractDocSearchIndexService implements
		DocSearchIndexService {
	/**
	 * This is a thread that will try to pull documents from the document store
	 * that are unindexed and index them. If it finds there are currently no
	 * unindexed documents, it will go to sleep for a little and then try again.
	 * 
	 * @author ivancich
	 * 
	 */
	class IndexPullingThread extends Thread {
		boolean shouldStop;

		public IndexPullingThread() {
			shouldStop = false;
		}

		public void requestStop() {
			shouldStop = true;
			this.interrupt();
		}

		void markAsErrorIndexing(String documentId, Throwable exception) {
			try {
				logger.error("Unable to index document: " + documentId,
						exception);
				docStoreService.markAsErrorIndexing(documentId);
			} catch (DocStoreException e) {
				logger.error(
						"Unable to mark document as having indexing error: "
								+ documentId, e);
			}
		}

		public void run() {
			while (!shouldStop) {
				String documentId;
				while (!shouldStop
						&& (documentId = docStoreService.nextUnindexed()) != null) {
					try {
						indexDocument(documentId);
					} catch (Exception e) {
						markAsErrorIndexing(documentId, e);
					}
				}

				try {
					Thread.sleep(unindexedDocSearchInterval);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.docsearch");

	/**
	 * The document storage service we're feeding off of.
	 */
	protected DocStoreService docStoreService;

	protected ContentAnalyzer contentAnalyzer;

	/**
	 * In milliseconds; non-positive values indicate that there is no automated
	 * query for un-indexed documents
	 */
	protected int unindexedDocSearchInterval;

	/**
	 * True if trying to shut down; this flag will prevent a new thread from
	 * being created while we're trying to shut down the current one.
	 */
	private boolean shuttingDown;

	/**
	 * The thread that polls the DocStoreService for unindexed documents.
	 */
	private IndexPullingThread indexPullingThread;

	public AbstractDocSearchIndexService(DocStoreService service,
			ContentAnalyzer analyzer) {
		setDocStoreService(service);
		setContentAnalyzer(analyzer);
		this.shuttingDown = false;
		this.unindexedDocSearchInterval = -1;
	}

	public AbstractDocSearchIndexService(DocStoreService service,
			ContentAnalyzer analyzer, int unindexedDocSearchInterval) {
		this(service, analyzer);
		this.unindexedDocSearchInterval = unindexedDocSearchInterval;
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

		// then they start up
		subStartup();

		// start the thread after they're up and running since we're calling
		// down into them
		managePullThread(unindexedDocSearchInterval);
	}

	@Override
	public final void shutdown() throws DocSearchException {
		// first I shut down my thread if I have one
		synchronized (this) {
			shuttingDown = true;
			if (indexPullingThread != null) {
				indexPullingThread.requestStop();
				try {
					indexPullingThread.join();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

		// then they shut down
		subShutdown();

		// then I shut down anything else
	}

	public abstract void doIndexDocument(String identifier)
			throws DocStoreException, DocSearchException;

	@Override
	public final void indexDocument(String identifier)
			throws DocStoreException, DocSearchException {
		doIndexDocument(identifier);
		docStoreService.markAsIndexed(identifier);
		logger.info("indexed document " + identifier);
	}

	@Override
	public void indexDocuments(Collection<String> identifiers)
			throws DocStoreException, DocSearchException {
		for (String identifier : identifiers) {
			indexDocument(identifier);
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
	public void setUnindexedDocSearchInterval(int milliseconds) {
		unindexedDocSearchInterval = milliseconds;
		managePullThread(milliseconds);
	}

	private synchronized void managePullThread(int milliseconds) {
		if (!shuttingDown) {
			if (milliseconds > 0 && indexPullingThread == null) {
				indexPullingThread = new IndexPullingThread();
				indexPullingThread.start();
			} else if (milliseconds <= 0 && indexPullingThread != null) {
				indexPullingThread.requestStop();
				indexPullingThread = null;
			}
		}
	}
}
