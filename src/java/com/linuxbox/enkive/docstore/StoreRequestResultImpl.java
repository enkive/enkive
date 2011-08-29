package com.linuxbox.enkive.docstore;

public class StoreRequestResultImpl implements StoreRequestResult {
	private String identifier;
	private boolean alreadyStored;
	private int shardKey;

	public StoreRequestResultImpl(String identifier, boolean alreadyStored,
			int shardKey) {
		this.identifier = identifier;
		this.alreadyStored = alreadyStored;
		this.shardKey = shardKey;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public boolean getAlreadyStored() {
		return alreadyStored;
	}

	@Override
	public int getShardKey() {
		return shardKey;
	}
}
