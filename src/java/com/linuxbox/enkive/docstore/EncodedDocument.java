package com.linuxbox.enkive.docstore;

import java.io.Reader;

public interface EncodedDocument extends Document {
	String getEncoding();
	
	char[] getEncodedContentChars();
	Reader getEncodedContentReader();
}
