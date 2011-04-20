package com.linuxbox.enkive.docstore;

import java.io.Reader;
import java.nio.charset.Charset;

public interface EncodedDocument extends Document {
	String getBinaryEncoding();
	Charset getCharset();
	
	char[] getEncodedContentChars();
	String getEncodedContentString();
	Reader getEncodedContentReader();
}
