package com.linuxbox.enkive.message;

import java.io.InputStream;

public interface EncodedContentReadData extends BaseContentReadData {
	public String getFilename();

	public String getMimeType();

	public String getUUID();

	public InputStream getEncodedContent() throws ContentException;
}
