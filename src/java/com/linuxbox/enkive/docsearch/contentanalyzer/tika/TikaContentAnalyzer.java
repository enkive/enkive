package com.linuxbox.enkive.docsearch.contentanalyzer.tika;

import java.io.IOException;
import java.io.Reader;

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public class TikaContentAnalyzer implements ContentAnalyzer {
	private static final String FILE_NAME_ROOT = "someFile.";
	private Tika tika;

	public TikaContentAnalyzer() {
		tika = new Tika();
	}

	@Override
	public Reader parseIntoText(Document document) throws IOException,
			DocStoreException {
		final Metadata metaData = new Metadata();
		metaData.set(Metadata.CONTENT_TYPE, document.getMimeType());
		metaData.set(Metadata.CONTENT_DISPOSITION,
				FILE_NAME_ROOT + document.getFileExtension());
		final Reader result = tika.parse(document.getDecodedContentStream(),
				metaData);

		// TODO consider doing something with metaData -- the parsing put stuff
		// in here! One possibility would be to turn it into a separate document
		// and index it as well or add it to the existing document, depending on
		// whether we wanted to search for core content and metadata together or
		// separately

		return result;
	}
}
