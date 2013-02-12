package com.linuxbox.enkive.message;

public interface EncodedContentReadData extends BaseContentReadData {
	public String getFilename();
	public String getMimeType();
	public String getUUID();
}
