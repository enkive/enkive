package com.linuxbox.enkive.normalization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides the core functionality for a chain of email address normalizers.
 * Classes that subclass this simply have to implement the myNormalize method.
 */
public abstract class AbstractChainedEmailAddressNormalizer extends
		AbstractEmailAddressNormalizer {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.normalization");

	private final EmailAddressNormalizer priorInChain;

	protected AbstractChainedEmailAddressNormalizer() {
		this(null);
	}

	protected AbstractChainedEmailAddressNormalizer(
			EmailAddressNormalizer priorInChain) {
		this.priorInChain = priorInChain;
	}

	/**
	 * Does two things:
	 * 
	 * 1. It handles the chaining of normalizers.
	 * 
	 * 2. If a normalizer throws an exception it returns the original email
	 * address.
	 */
	@Override
	public String map(final String emailAddress) {
		try {
			String partiallyNormalized = emailAddress;
			if (null != priorInChain) {
				partiallyNormalized = priorInChain.map(emailAddress);
			}
			final String fullyNormalized = myMap(partiallyNormalized);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("\"" + emailAddress + "\" normalized to \""
						+ fullyNormalized + "\"");
			}
			return fullyNormalized;
		} catch (Exception e) {
			LOGGER.error("had problem normalizing email address \""
					+ emailAddress + "\"; keeping unchanged", e);
			return emailAddress;
		}
	}

	/**
	 * Subclasses will do their normalization in this method.
	 */
	protected abstract String myMap(String emailAddress);
}
