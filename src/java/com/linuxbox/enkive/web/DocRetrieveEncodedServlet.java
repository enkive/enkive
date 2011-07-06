package com.linuxbox.enkive.web;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class DocRetrieveEncodedServlet extends AbstractDocRetrieveServlet {
	private static final long serialVersionUID = -3951922119641775086L;

	@Override
	protected InputStream getInputStream(Document doc) throws DocStoreException {
		return doc.getEncodedContentStream();
	}

	@Override
	protected String getContentType(Document doc) {
		return "text/plain";
	}
}
