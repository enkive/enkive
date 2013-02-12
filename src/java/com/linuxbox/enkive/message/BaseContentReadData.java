package com.linuxbox.enkive.message;

import java.io.InputStream;

import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface BaseContentReadData {

	/**
	 * 
	 * @return a string containing a hexadecimal number of 20 bytes/160 bits/40
	 *         characters. The string has no spaces and all base-16 digits are
	 *         in lower case (i.e., a-f and not A-F). For example, a string
	 *         returned could be "2fd4e1c67a2d28fced849ee1bb76e7391b93eb12".
	 */
	String getSha1String();

	/**
	 * 
	 * @return An InputStream that can be used to retrieve the entire binary
	 *         content of the attachment/text. Given that MIME-encoded content
	 *         ultimately is sent as text-encoded (e.g., base64), this will be
	 *         the binary version of that.
	 */
	InputStream getBinaryContent() throws DocStoreException;
}