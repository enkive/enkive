package com.linuxbox.enkive.normalization;

/**
 * Provides the core functionality for a chain of email address normalizers.
 * Classes that subclass this simply have to implement the myNormalize method.
 */
public abstract class AbstractChainedEmailAddressNormalizer extends
		AbstractEmailAddressNormalizer {
	private final EmailAddressNormalizer priorInChain;

	protected AbstractChainedEmailAddressNormalizer() {
		this(null);
	}

	protected AbstractChainedEmailAddressNormalizer(
			EmailAddressNormalizer priorInChain) {
		this.priorInChain = priorInChain;
	}

	@Override
	public String map(String emailAddress) {
		if (null != priorInChain) {
			emailAddress = priorInChain.map(emailAddress);
		}
		return myNormalize(emailAddress);
	}

	protected abstract String myNormalize(String emailAddress);
}
