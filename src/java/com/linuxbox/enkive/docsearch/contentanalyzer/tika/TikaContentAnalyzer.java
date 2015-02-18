/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
