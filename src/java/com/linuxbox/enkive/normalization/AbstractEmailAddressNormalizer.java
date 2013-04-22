package com.linuxbox.enkive.normalization;

import com.linuxbox.util.CollectionUtils.Mapper;

/**
 * Provides useful utility routines and other partial implementation help.
 */
public abstract class AbstractEmailAddressNormalizer implements
		EmailAddressNormalizer, Mapper<String, String> {
	
	static protected String[] splitAddress(String emailAddress) {
		return emailAddress.split("@", 2);
	}
}
