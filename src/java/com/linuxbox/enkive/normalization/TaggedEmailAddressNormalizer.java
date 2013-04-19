package com.linuxbox.enkive.normalization;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Required;

/**
 * Some emailers allow for tags to be added to addresses and yet still be
 * considered to be the same address. For example, if one's email address is
 * pat@example.com, email sent to pat+tag1@example.com or pat+tag2@example.com
 * to also be delivered as well. This allows for the tracing of how an email
 * address is distributed, possibly clarifying organizations that sell email
 * addresses. In this case "+" is the tag delimiter. But other emailers allow
 * other characters, such as "-" to be tag delimiters.
 * 
 * This normalizer removes all characters in the local part starting at the
 * first tag delimiter.
 * 
 * For additional information, see:
 * http://en.wikipedia.org/wiki/Email_address#Address_tags
 */
public class TaggedEmailAddressNormalizer extends
		AbstractChainedEmailAddressNormalizer {
	protected char tagDelimiter;
	protected Pattern dotRegExp;

	public TaggedEmailAddressNormalizer(EmailAddressNormalizer prior) {
		super(prior);
	}

	public TaggedEmailAddressNormalizer() {
		this(null);
	}

	@Override
	protected String myNormalize(String emailAddress) {
		Matcher m = dotRegExp.matcher(emailAddress);
		String result = m.replaceFirst("@");
		return result.toString();
	}

	@Required
	public void setTagDelimiter(char tagDelimiter) {
		this.tagDelimiter = tagDelimiter;
		dotRegExp = Pattern.compile("\\" + tagDelimiter + "[^@]*@");
	}
}
