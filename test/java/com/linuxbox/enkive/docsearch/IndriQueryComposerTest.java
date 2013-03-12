/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.docsearch;

import static com.linuxbox.enkive.docsearch.indri.IndriQueryComposer.composeQuery;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Test;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public class IndriQueryComposerTest {
	final String s1 = "the rain in spain";

	@Test
	public void testNotice() throws DocSearchException {
		fail("not current; everything will likely fail here");
	}

	@Test
	public void testSimple() throws DocSearchException {
		final String indriQuery = composeQuery(s1).toString();
		assertEquals("#band<the rain in spain>", indriQuery);
	}

	@Test
	public void testNot() throws DocSearchException {
		final String indriQuery = composeQuery("-the rain in -spain")
				.toString();
		assertEquals("#band<#not<the> rain in #not<spain>>", indriQuery);
	}

	@Test
	public void testPhrase() throws DocSearchException {
		final String indriQuery = composeQuery("\"the rain in spain\"")
				.toString();
		assertEquals("#band<#1<the rain in spain>>", indriQuery);
	}

	@Test
	public void testNegatedPhrase() throws DocSearchException {
		final String indriQuery = composeQuery("-\"the rain in spain\"")
				.toString();
		assertEquals("#band<#not<#1<the rain in spain>>>", indriQuery);
	}

	@Test
	public void testPhrases() throws DocSearchException {
		final String indriQuery = composeQuery(
				"\"the rain in spain\" stays mainly \"in the plains\"")
				.toString();
		assertEquals(
				"#band<#1<the rain in spain> stays mainly #1<in the plains>>",
				indriQuery);
	}

	@Test
	public void testNegatedPhrases() throws DocSearchException {
		final String indriQuery = composeQuery(
				"-\"the rain in spain\" stays mainly -\"in the plains\"")
				.toString();
		assertEquals(
				"#band<#not<#1<the rain in spain>> stays mainly #not<#1<in the plains>>>",
				indriQuery);
	}
}
