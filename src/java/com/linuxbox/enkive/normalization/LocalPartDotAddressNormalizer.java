package com.linuxbox.enkive.normalization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LocalPartDotAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	final static Pattern DOT_RE =  Pattern.compile("\\."); 
	
	public LocalPartDotAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);
	}

	public LocalPartDotAddressNormalizer() {
		this(null);
	}

	@Override
	protected String myNormalize(String emailAddress) {
		final String[] parts = splitAddress(emailAddress);
		final String localPart = parts[0];
		final String domainPart = parts[1];

		StringBuilder result = new StringBuilder();
		
		Matcher m = DOT_RE.matcher(localPart);
		result.append(m.replaceAll(""));
		result.append('@');
		result.append(domainPart);
		
		return result.toString();
	}
	
	/**
	 * A little testing.
	 */
	public static void main(String[] args) {
		EmailAddressNormalizer n = new LocalPartDotAddressNormalizer();
		System.out.println(n.normalize("elizabeth.zephyr@gmail.com"));
		System.out.println(n.normalize("dash.dash.dash.dash..dash@gmail.com"));
		System.out.println(n.normalize("...surround...@gmail.com"));
	}
}
