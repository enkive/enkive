package com.linuxbox.enkive.docsearch.indri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.QueryRequest;
import lemurproject.indri.QueryResult;
import lemurproject.indri.QueryResults;
import lemurproject.indri.ScoredExtentResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.AbstractSearchService;
import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.util.CollectionUtils;
import com.linuxbox.util.StreamConnector;
import com.linuxbox.util.TypeConverter;

public class IndriSearchService extends AbstractSearchService {
	/**
	 * This is supposed to be a callback that gets status updates. But as of
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

	/**
	 * Converts a QueryResult into the docno.
	 * 
	 * @author ivancich
	 * 
	 */
	static class QueryResultToDocNameConverter implements
			TypeConverter<QueryResult, String> {
		@Override
		public String convert(QueryResult value) throws Exception {
			return (String) value.metadata.get(NAME_FIELD);
		}
	}

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	private static final boolean STORE_DOCUMENTS = false;
	private static final String NAME_FIELD = "docno";
	private static final String[] METADATA_FIELDS = { NAME_FIELD };

	private static final long MEMORY_TO_USE = 200 * 1024 * 1024; // 200 MB
	private static final String STEMMER = "krovetz";

	private static final String TEXT_FORMAT = "txt";
	private static final String TRECTEXT_FORMAT = "trectext";
	private static final long DOC_SIZE_IN_MEMORY_LIMIT = 8 * 1024; // 8 KB
	// private static final IndexStorage INDEX_STORAGE = IndexStorage.BY_SIZE;
	// private static final IndexStorage INDEX_STORAGE =
	// IndexStorage.PARSED_DOCUMENT;
	private static final IndexStorage INDEX_STORAGE = IndexStorage.FILE;
	private static final QueryResultToDocNameConverter QUERY_RESULT_CONVERTER = new QueryResultToDocNameConverter();

	private String repositoryPath;
	private File tempStorageDir;

	private IndexEnvironment indexEnvironment;
	private QueryEnvironment queryEnvironment;

	public IndriSearchService(DocStoreService docStoreService,
			ContentAnalyzer analyzer, String repositoryPath,
			String temporaryStoragePath) throws DocSearchException {
		super(docStoreService, analyzer);
		finishConstruction(repositoryPath, temporaryStoragePath);
	}

	public IndriSearchService(DocStoreService docStoreService,
			ContentAnalyzer analyzer, String repositoryPath,
			String temporaryStoragePath, int unindexedDocSearchInterval)
			throws DocSearchException {
		super(docStoreService, analyzer, unindexedDocSearchInterval);
		finishConstruction(repositoryPath, temporaryStoragePath);
	}

	@Override
	public void startup() {
		// empty
	}

	@Override
	public void shutdown() throws DocSearchException {
		Exception e = null;

		if (queryEnvironment != null) {
			try {
				queryEnvironment.close();
			} catch (Exception e1) {
				e = e1;
			}
		}

		if (indexEnvironment != null) {
			try {
				indexEnvironment.close();
			} catch (Exception e1) {
				e = e1;
			}
		}

		if (e != null) {
			throw new DocSearchException(
					"could not shut down INDRI search service", e);
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

	private static void initializeIndexEnvironment(
			IndexEnvironment indexEnvironment) throws DocSearchException {
		try {
			indexEnvironment.setStoreDocs(STORE_DOCUMENTS);
			indexEnvironment.setStemmer(STEMMER);
			indexEnvironment.setIndexedFields(METADATA_FIELDS);
			indexEnvironment.setMetadataIndexedFields(METADATA_FIELDS,
					METADATA_FIELDS);
			indexEnvironment.setNormalization(true);

			if (MEMORY_TO_USE > 0) {
				indexEnvironment.setMemory(MEMORY_TO_USE);
			}
		} catch (Exception e) {
			throw new DocSearchException(
					"could not initialize INDRI index environment", e);
		}
	}

	private void finishConstruction(String repositoryPath,
			String temporaryStoragePath) throws DocSearchException {
		initializeTemporaryStorage(temporaryStoragePath);

		this.repositoryPath = repositoryPath;
		indexEnvironment = new IndexEnvironment();
		initializeIndexEnvironment(indexEnvironment);

		// try to open existing repository; if that fails try to create one and
		// open it
		try {
			indexEnvironment.open(repositoryPath);
		} catch (Exception e1) {
			createIndriRepository(indexEnvironment, repositoryPath);
			try {
				indexEnvironment.open(repositoryPath);
			} catch (Exception e2) {
				throw new DocSearchException(
						"could not open the INDRI repository", e2);
			}
		}

		try {
			queryEnvironment = new QueryEnvironment();
			queryEnvironment.addIndex(repositoryPath);
		} catch (Exception e1) {
			throw new DocSearchException(
					"could not create an INDRI query environment", e1);
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
			System.out.println(docString.toString());
			final int documentId = indexEnvironment.addString(
					docString.toString(), TEXT_FORMAT, metaData);
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

				indexEnvironment.addFile(tempFile.getAbsolutePath(),
						TRECTEXT_FORMAT);
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
			final int docId = indexEnvironment.addParsedDocument(parsedDoc);
			return docId;
		} catch (Exception e) {
			throw new DocSearchException("could not indexed ParsedDocument "
					+ identifier);
		}
	}

	@Override
	public void doIndexDocument(String identifier) throws DocSearchException,
			DocStoreException {
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
		} catch (DocStoreException e) {
			throw e;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("errors managing INDRI index", e);
		}
	}

	@Override
	public List<String> search(String query, int maxResults)
			throws DocSearchException {
		try {
			final ScoredExtentResult[] results = queryEnvironment.runQuery(
					query, maxResults);
			String[] resultDocNumbers = queryEnvironment.documentMetadata(
					results, NAME_FIELD);
			return CollectionUtils.listFromArray(resultDocNumbers);
		} catch (Exception e) {
			throw new DocSearchException("could not perform INDRI query", e);
		}
	}

	/**
	 * This is an alternate implementation of search. It fails when the document
	 * is not stored in the INDRI database.
	 * 
	 * @param query
	 * @param maxResults
	 * @return
	 * @throws DocSearchException
	 */
	@SuppressWarnings("unused")
	private List<String> searchAlt(String query, int maxResults)
			throws DocSearchException {
		try {
			QueryRequest request = new QueryRequest();
			request.query = query;
			request.startNum = 0;
			request.resultsRequested = maxResults;
			request.metadata = METADATA_FIELDS;

			// NB: this call will result in an exception if INDRI does not store
			// a compressed version of the documents
			QueryResults queryResults = queryEnvironment.runQuery(request);

			return CollectionUtils.listFromConvertedArray(queryResults.results,
					QUERY_RESULT_CONVERTER);
		} catch (ClassCastException e) {
			throw new DocSearchException(
					"could not retrieve document identifer from INDRI query");
		} catch (Exception e) {
			throw new DocSearchException("could not perform INDRI query", e);
		}
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}
}
