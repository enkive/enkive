package com.linuxbox.enkive.normalization;

import com.linuxbox.util.CollectionUtils.Mapper;

/**
 * This bean is designed to be a common place to normalize email addresses.
 */
public interface EmailAddressNormalizer extends Mapper<String, String> {
	/**
	 * Returns a normalized email address given another email address.
	 */
	String map(String originalEmailAddress);
}
