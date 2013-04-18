package com.linuxbox.enkive.normalization;

import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Required;

public class TaggedEmailAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	
	protected char tagDelimiter;
	
	public TaggedEmailAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);
	}
	
	public TaggedEmailAddressNormalizer() {
		this();
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
	
	@Required
	public void setTagDelimiter(char tagDelimiter) {
		this.tagDelimiter = tagDelimiter;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
