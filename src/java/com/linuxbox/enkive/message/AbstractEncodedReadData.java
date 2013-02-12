package com.linuxbox.enkive.message;

public abstract class AbstractEncodedReadData implements EncodedContentReadData {

	protected String uuid;
	protected String filename;
	protected String mimeType;
	
	public AbstractEncodedReadData() {
		// empty
	}

	public AbstractEncodedReadData(String uuid, String filename, String mimeType) {
		this.uuid = uuid;
		this.filename = filename;
		this.mimeType = mimeType;
	}

	public String getUUID() {
		return uuid;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getSha1String() {
		return uuid;
	}
}