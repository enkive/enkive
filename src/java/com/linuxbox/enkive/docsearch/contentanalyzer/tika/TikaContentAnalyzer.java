package com.linuxbox.enkive.docsearch.contentanalyzer.tika;

import java.io.IOException;
import java.io.Reader;

import org.apache.tika.Tika;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class TikaContentAnalyzer implements ContentAnalyzer {
	Tika tika;

	public TikaContentAnalyzer() {
		tika = new Tika();
	}

	@Override
	public Reader parseIntoText(Document document) throws IOException,
			DocStoreException {
		return tika.parse(document.getContentStream());
	}
}
