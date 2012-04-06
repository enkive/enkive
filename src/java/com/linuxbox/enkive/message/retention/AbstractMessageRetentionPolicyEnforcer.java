package com.linuxbox.enkive.message.retention;


public abstract class AbstractMessageRetentionPolicyEnforcer implements
		MessageRetentionPolicyEnforcer {

	protected MessageRetentionPolicy retentionPolicy;
	
	@Override
	public MessageRetentionPolicy getRetentionPolicy() {
		return retentionPolicy;
	}

	@Override
	public void setRetentionPolicy(MessageRetentionPolicy retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

}
