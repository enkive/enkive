package com.linuxbox.enkive.normalization;

public class CaseEmailAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	protected boolean caseFoldDomainPart = true;

	public CaseEmailAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);
	}

	public CaseEmailAddressNormalizer() {
		this(null);
	}

	/**
	 * The assumption is that the address is valid; no additional checking is
	 * done. For example, it is assumed there is exactly one "@" in the address
	 * to separate the local part from the domain part.
	 */
	@Override
	protected String myNormalize(String emailAddress) {
		final String[] parts = splitAddress(emailAddress);
		final String localPart = parts[0];
		final String domainPart = parts[1];

		final StringBuilder result = new StringBuilder();
		result.append(localPart.toLowerCase());
		result.append('@');
		if (caseFoldDomainPart) {
			result.append(domainPart.toLowerCase());
		} else {
			result.append(domainPart);
		}

		return result.toString();
	}

	public void setCaseFoldDomainPart(boolean caseFoldDomainPart) {
		this.caseFoldDomainPart = caseFoldDomainPart;
	}
}
