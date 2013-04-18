package com.linuxbox.enkive.normalization;

/**
 * Provides useful utility routines and other partial implmentation help.
 */
public abstract class AbstractEmailAddressNormalizer implements
		EmailAddressNormalizer {
	static protected String[] splitAddress(String emailAddress) {
		return emailAddress.split("@", 2);
	}
}
