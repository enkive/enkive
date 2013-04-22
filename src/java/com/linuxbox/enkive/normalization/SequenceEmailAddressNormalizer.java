package com.linuxbox.enkive.normalization;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class SequenceEmailAddressNormalizer implements EmailAddressNormalizer {
	protected List<EmailAddressNormalizer> normalizerList;

	@Override
	public String map(String emailAddress) {
		for (EmailAddressNormalizer normalizer : normalizerList) {
			emailAddress = normalizer.map(emailAddress);
		}

		return emailAddress;
	}

	public List<EmailAddressNormalizer> getNormalizers() {
		return normalizerList;
	}

	@Required
	public void setNormalizerList(List<EmailAddressNormalizer> normalizerList) {
		this.normalizerList = normalizerList;
	}
}
