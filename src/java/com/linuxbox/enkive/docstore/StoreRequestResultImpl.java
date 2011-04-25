package com.linuxbox.enkive.docstore;

public class StoreRequestResultImpl implements StoreRequestResult {
	private String identifier;
	private boolean alreadyStored;

	public StoreRequestResultImpl(String identifier, boolean alreadyStored) {
		this.identifier = identifier;
		this.alreadyStored = alreadyStored;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public boolean getAlreadyStored() {
		return alreadyStored;
	}
}
