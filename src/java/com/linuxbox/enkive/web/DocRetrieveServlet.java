package com.linuxbox.enkive.web;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

/**
 * Subclass of AbstractDocRetrieveServlet that returns the decoded (binary)
 * stream.
 * 
 * @author eric
 * 
 */
public class DocRetrieveServlet extends AbstractDocRetrieveServlet {
	private static final long serialVersionUID = 7346718717915724502L;

	@Override
	protected InputStream getInputStream(Document doc) throws DocStoreException {
		return doc.getDecodedContentStream();
	}

	@Override
	protected String getContentType(Document doc) {
		return doc.getMimeType();
	}

	@Override
	protected void handleResponse(HttpServletResponse response, Document doc) {
		response.addHeader("Content-disposition", "attachment; filename="
				+ System.currentTimeMillis() + "." + doc.getFileExtension());
	}
}
