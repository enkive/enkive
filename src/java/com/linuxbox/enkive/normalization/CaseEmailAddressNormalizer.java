package com.linuxbox.enkive.normalization;

/**
 * The domain part of an address should be compared in a case insensitive
 * manner. So this normalizer converts the domain part to all-lower case (since
 * that seems to be more typical that all upper-case domain names). According to
 * the standards, the local part of the email address is case sensitive. However
 * most organizations treat the local part as case insensitive.
 * 
 * This normalizers converts the domain part to lower-case. The local part, by
 * default, is also converted to lower case, although that behavior can be
 * controlled via the setCaseFoldLocalPart setter.
 * 
 * For more information, see: http://en.wikipedia.org/wiki/Email_address
 */
public class CaseEmailAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	protected boolean caseFoldLocalPart;

	public CaseEmailAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);

		// by default we treat the local part as case insensitive, although this
		// can be altered through the setter
		caseFoldLocalPart = true;
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

		if (caseFoldLocalPart) {
			result.append(localPart.toLowerCase());
		} else {
			result.append(localPart);
		}
		result.append('@');
		result.append(domainPart.toLowerCase());

		return result.toString();
	}

	public void setCaseFoldLocalPart(boolean caseFoldLocalPart) {
		this.caseFoldLocalPart = caseFoldLocalPart;
	}
}
