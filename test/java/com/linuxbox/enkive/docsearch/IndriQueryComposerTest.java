package com.linuxbox.enkive.docsearch;

import static com.linuxbox.enkive.docsearch.indri.IndriQueryComposer.composeQuery;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;

public class IndriQueryComposerTest {
	final String s1 = "the rain in spain";

	@Test
	public void testSimple() {
		final String indriQuery = composeQuery(s1).toString();
		assertEquals("#band<the rain in spain>", indriQuery);
	}

	@Test
	public void testNot() {
		final String indriQuery = composeQuery("-the rain in -spain")
				.toString();
		assertEquals("#band<#not<the> rain in #not<spain>>", indriQuery);
	}

	@Test
	public void testPhrase() {
		final String indriQuery = composeQuery("\"the rain in spain\"")
				.toString();
		assertEquals("#band<#1<the rain in spain>>", indriQuery);
	}

	@Test
	public void testNegatedPhrase() {
		final String indriQuery = composeQuery("-\"the rain in spain\"")
				.toString();
		assertEquals("#band<#not<#1<the rain in spain>>>", indriQuery);
	}

	@Test
	public void testPhrases() {
		final String indriQuery = composeQuery(
				"\"the rain in spain\" stays mainly \"in the plains\"")
				.toString();
		assertEquals(
				"#band<#1<the rain in spain> stays mainly #1<in the plains>>",
				indriQuery);
	}

	@Test
	public void testNegatedPhrases() {
		final String indriQuery = composeQuery(
				"-\"the rain in spain\" stays mainly -\"in the plains\"")
				.toString();
		assertEquals(
				"#band<#not<#1<the rain in spain>> stays mainly #not<#1<in the plains>>>",
				indriQuery);
	}
}
