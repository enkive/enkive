package com.linuxbox.enkive.message.retention;


public interface MessageRetentionPolicyEnforcer {
	// Needs to find messages that are outside of the retention policy
	// and flag them for deletion
	public void enforceMessageRetentionPolicies();

	public MessageRetentionPolicy getRetentionPolicy();

	public void setRetentionPolicy(MessageRetentionPolicy messageRetentionPolicy);

}
