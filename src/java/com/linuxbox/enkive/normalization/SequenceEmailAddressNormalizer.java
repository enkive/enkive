package com.linuxbox.enkive.normalization;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

public class SequenceEmailAddressNormalizer implements EmailAddressNormalizer {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.normalization");

	protected List<EmailAddressNormalizer> normalizerList;

	@Override
	public String map(final String emailAddress) {
		try {
			String normalizingEmailAddress = emailAddress;
			for (EmailAddressNormalizer normalizer : normalizerList) {
				normalizingEmailAddress = normalizer
						.map(normalizingEmailAddress);
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("\"" + emailAddress + "\" normalized to \""
						+ normalizingEmailAddress + "\"");
			}

			return normalizingEmailAddress;
		} catch (Exception e) {
			LOGGER.error("had problem normalizing email address \""
					+ emailAddress + "\"; keeping unchanged", e);
			return emailAddress;
		}
	}

	public List<EmailAddressNormalizer> getNormalizers() {
		return normalizerList;
	}

	@Required
	public void setNormalizerList(List<EmailAddressNormalizer> normalizerList) {
		this.normalizerList = normalizerList;
	}
}
