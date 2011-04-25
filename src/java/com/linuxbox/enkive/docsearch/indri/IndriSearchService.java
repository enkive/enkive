package com.linuxbox.enkive.docsearch.indri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;
import lemurproject.indri.QueryEnvironment;

import com.linuxbox.enkive.docsearch.AbstractSearchService;
import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.linuxbox.util.StreamConnector;

public class IndriSearchService extends AbstractSearchService {
	private static final boolean STORE_DOCUMENTS = false;
	private static final long MEMORY_TO_USE = 200 * 1024 * 1024; // 200 MB
	private static final String STEMMER = "krovetz";

	private static final String TRECTEXT = "trectext";
	private static final long DOC_SIZE_IN_MEMORY_LIMIT = 8 * 1024; // 8 KB
	private static final boolean DISPLAY_STATS = true;

	private String repositoryPath;
	private File tempStorageDir;

	private IndexEnvironment indexEnvironment;
	private QueryEnvironment queryEnvironment;

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

	private void createIndriRepository(String repositoryPath)
			throws DocSearchException {
		try {
			IndexEnvironment indexEnvironment = new IndexEnvironment();
			indexEnvironment.create(repositoryPath);
			indexEnvironment.close();
		} catch (Exception e) {
			throw new DocSearchException(
					"could not construct an INDRI repository", e);
		}
	}

	private void finishConstruction(String repositoryPath,
			String temporaryStoragePath) throws DocSearchException {
		initializeTemporaryStorage(temporaryStoragePath);

		this.repositoryPath = repositoryPath;

		// currently not used
		RecordedIndexStatus indexStatus = new RecordedIndexStatus();

		indexEnvironment = new IndexEnvironment();

		// try to open existing repository; if that fails try to create one
		try {
			indexEnvironment.open(repositoryPath, indexStatus);
			if (DISPLAY_STATS) {
				indexStatus.display(System.out);
			}
		} catch (Exception e1) {
			createIndriRepository(repositoryPath);
			try {
				indexEnvironment.open(repositoryPath, indexStatus);
				if (DISPLAY_STATS) {
					indexStatus.display(System.out);
				}
			} catch (Exception e2) {
				throw new DocSearchException(
						"could not open the INDRI repository", e2);
			}
		}

		try {
			indexEnvironment.setStoreDocs(STORE_DOCUMENTS);
			if (MEMORY_TO_USE > 0) {
				indexEnvironment.setMemory(MEMORY_TO_USE);
			}
			indexEnvironment.setStemmer(STEMMER);
		} catch (Exception e) {
			try {
				indexEnvironment.close();
			} catch (Exception e2) {
				// empty
			} finally {
				indexEnvironment = null;
			}

			throw new DocSearchException(
					"could not initialize INDRI index environment", e);
		}

		/*
		 * 
		 * try { queryEnvironment = new QueryEnvironment();
		 * queryEnvironment.addIndex(repositoryPath); } catch (Exception e1) {
		 * try { indexEnvironment.close(); } catch (Exception e2) { // empty }
		 * finally { indexEnvironment = null; }
		 * 
		 * throw new DocSearchException(
		 * "could not create an INDRI query environment", e1); }
		 */
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

	private void indexDocumentAsString(IndexEnvironment indexEnvironment,
			Document doc) throws DocStoreException, DocSearchException {
		try {
			StringBuilder docString = new StringBuilder();
			Reader input = contentAnalyzer.parseIntoText(doc);
			char buffer[] = new char[4096];

			int charsRead;

			while ((charsRead = input.read(buffer)) > 0) {
				docString.append(buffer, 0, charsRead);
			}

			Map<String, String> metaData = new HashMap<String, String>();

			metaData.put("DOCNO", doc.getIdentifier());
			System.out.println(docString.toString());
			indexEnvironment.addString(docString.toString(), "text", metaData);
		} catch (DocStoreException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("could not index \""
					+ doc.getIdentifier() + "\"", e);
		}
	}

	private void indexDocumentAsFile(IndexEnvironment indexEnvironment,
			Document doc) throws DocSearchException, DocStoreException {
		final String identifier = doc.getIdentifier();
		Reader input = null;
		try {
			input = contentAnalyzer.parseIntoText(doc);

			File tempFile = File.createTempFile("enkive-indri", ".txt",
					tempStorageDir);
			PrintWriter output = null;
			try {
				output = new PrintWriter(new FileWriter(tempFile));
				output.println("<DOC>");
				output.println("<DOCNO>" + identifier + "</DOCNO>");
				output.println("<TEXT>");
				StreamConnector.transferForeground(input, output);
				output.println("</TEXT>");
				output.println("</DOC>");
				output.close();

				System.out.println("Try to access "
						+ tempFile.getAbsolutePath());

				indexEnvironment.addFile(tempFile.getAbsolutePath(), TRECTEXT);
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

	@Override
	public void doIndexDocument(String identifier) throws DocSearchException,
			DocStoreException {
		IndexEnvironment ie = null;
		try {
			Document doc = docStoreService.retrieve(identifier);
			ie = new IndexEnvironment();
			ie.open(repositoryPath);
			if (false) {
				if (false) {
					System.err.println("Warning: forcing indexing document as string");
					indexDocumentAsString(ie, doc);
				} else {
					System.err.println("Warning: forcing indexing document as file");
					indexDocumentAsFile(ie, doc);
				}
			} else if (doc.getSize() > DOC_SIZE_IN_MEMORY_LIMIT) {
				indexDocumentAsFile(ie, doc);
			} else {
				indexDocumentAsString(ie, doc);
			}
		} catch (DocStoreException e) {
			throw e;
		} catch (DocSearchException e) {
			throw e;
		} catch (Exception e) {
			throw new DocSearchException("errors managing INDRI index", e);
		} finally {
			try {
				if (ie != null) {
					ie.close();
				}
			} catch (Exception e) {
				// empty
			}
		}
	}

	@Override
	public List<String> search(String query) {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public List<String> search(String query, int maxResults) {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
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

	public String getRepositoryPath() {
		return repositoryPath;
	}
}
