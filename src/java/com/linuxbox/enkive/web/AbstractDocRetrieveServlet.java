package com.linuxbox.enkive.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.util.StreamConnector;

public abstract class AbstractDocRetrieveServlet extends EnkiveServlet {
	private static final long serialVersionUID = 2292532161370976431L;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doGet(req, resp);
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		final String documentId = req.getParameter("document_id");
		final DocStoreService docStoreService = getDocStoreService();
		
		try {
			final Document doc = docStoreService.retrieve(documentId);
			LOGGER.trace("retrieving document " + documentId);
			
			resp.setContentType(getContentType(doc));

			final InputStream inputStream = getInputStream(doc);
			final ServletOutputStream outputStream = resp.getOutputStream();
			StreamConnector.transferForeground(inputStream, outputStream);

			resp.flushBuffer();
		} catch (DocumentNotFoundException e) {
			LOGGER.error("failed to retrieve document " + documentId, e);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} catch (DocStoreException e) {
			LOGGER.error("error retrieving document " + documentId, e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Some implementations of this abstract class will return the decoded
	 * stream, others the encoded stream.
	 * 
	 * @param doc
	 * @return
	 * @throws DocStoreException
	 */
	protected abstract InputStream getInputStream(Document doc)
			throws DocStoreException;

	/**
	 * Returns the mime type of the result.
	 * 
	 * @param doc
	 * @return
	 * @throws DocStoreException
	 */
	protected abstract String getContentType(Document doc);
}