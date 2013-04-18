package com.linuxbox.enkive.normalization;

/**
 * This bean is designed to be a common place to normalize email addresses.
 */
public interface EmailAddressNormalizer {
	String normalize(String emailAddress);
}
