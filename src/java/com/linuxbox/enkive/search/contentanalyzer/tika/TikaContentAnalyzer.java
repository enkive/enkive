package com.linuxbox.enkive.search.contentanalyzer.tika;

import java.io.IOException;
import java.io.Reader;

import org.apache.tika.Tika;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.search.contentanalyzer.ContentAnalyzer;

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
