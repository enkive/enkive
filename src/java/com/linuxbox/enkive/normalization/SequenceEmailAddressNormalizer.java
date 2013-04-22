package com.linuxbox.enkive.normalization;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

public class SequenceEmailAddressNormalizer implements EmailAddressNormalizer {
	protected List<EmailAddressNormalizer> normalizers;

	@Override
	public String map(String emailAddress) {
		for (EmailAddressNormalizer normalizer : normalizers) {
			emailAddress = normalizer.map(emailAddress);
		}

		return emailAddress;
	}

	public List<EmailAddressNormalizer> getNormalizers() {
		return normalizers;
	}

	@Required
	public void setNormalizers(List<EmailAddressNormalizer> normalizers) {
		this.normalizers = normalizers;
	}
}
