/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
 *******************************************************************************/
package com.linuxbox.enkive.docsearch.indri;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.TextQueryParser;
import com.linuxbox.enkive.docsearch.TextQueryParser.Phrase;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public class IndriQueryComposer {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch.indri");

	protected static Set<Character> allowableSymbols = new HashSet<Character>();

	static {
		allowableSymbols.add('.');
		allowableSymbols.add('@');
		allowableSymbols.add('-');
		allowableSymbols.add('_');
	}

	public static CharSequence composeQuery(String queryString)
			throws DocSearchException {
		return composeQuery(TextQueryParser.parseContentCriteria(queryString));
	}

	/**
	 * Returns a phrase in Indri's query language #N(...) construct, where N is
	 * the size of the window. Since we want zero words to be between the words
	 * in our phrase, we use N=1 (you'd think it should be N=0, but that's not
	 * how Indri is set up.
	 * 
	 * @param phrase
	 * @return
	 */
	protected static CharSequence composePhrase(Phrase phrase) {
		StringBuffer result = new StringBuffer();

		if (phrase.hasMultipleTerms()) {
			result.append("#1(");
			result.append(phrase.getTermsAsCharSeq());
			result.append(")");
		} else {
			result.append(phrase.getTermsAsCharSeq());
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("composePhrase returns: " + result);
		}

		return result;
	}

	/**
	 * Sanitize search term by only allowing letters, digits, and a small subset
	 * of symbols.
	 * 
	 * @param buffer
	 */
	protected static void sanitizeStringBuffer(StringBuffer buffer) {
		for (int i = buffer.length() - 1; i >= 0; i--) {
			Character c = buffer.charAt(i);
			if (!Character.isLetterOrDigit(c) && !allowableSymbols.contains(c)) {
				buffer.deleteCharAt(i);
			}
		}
	}

	/**
	 * Iterate through all the terms in the phrase and sanitize each one in
	 * place.
	 * 
	 * @param phrase
	 */
	protected static void sanitizePhraseInPlace(Phrase phrase) {
		ListIterator<CharSequence> i = phrase.getTermsListIterator();
		while (i.hasNext()) {
			CharSequence charSeq = i.next();

			// convert to a StringBuffer if not one already
			StringBuffer buffer;
			if (!(charSeq instanceof StringBuffer)) {
				buffer = new StringBuffer(charSeq);
			} else {
				buffer = (StringBuffer) charSeq;
			}

			sanitizeStringBuffer(buffer);

			// replace term in phrase
			i.set(buffer);
		}
	}

	/**
	 * Creates an Indri query by using the filter-require (#filreq) and
	 * filter-reject (#filrej) operators to filter the results. Then it uses the
	 * #compose operator to rank/weight the result of the the required elements.
	 * If there are no required elements, it uses a simple #not to complete the
	 * query.
	 * 
	 * @param query
	 * @return
	 * @throws DocSearchException
	 */
	public static CharSequence composeQuery(TextQueryParser.Query query)
			throws DocSearchException {
		if (LOGGER.isTraceEnabled()) {
			StringBuilder output = new StringBuilder();
			boolean first = true;
			for (Phrase p : query) {
				if (first) {
					first = false;
				} else {
					output.append(", ");
				}
				output.append(p.getTermsAsCharSeq());
			}
			LOGGER.trace("composeQuery receives phrases: " + output.toString());
		}

		StringBuffer result = new StringBuffer();

		/*
		 * Separate the requirements and the rejections and sanitize in place.
		 */
		List<Phrase> requirements = new LinkedList<Phrase>();
		List<Phrase> rejections = new LinkedList<Phrase>();
		for (Phrase p : query) {
			sanitizePhraseInPlace(p);

			if (p.isNegated()) {
				rejections.add(p);
			} else {
				requirements.add(p);
			}
		}

		if (!requirements.isEmpty()) {
			// prefer to combine requirements with #combine, which does some
			// perhaps useful weighting of search terms based on frequency
			if (requirements.size() > 1) {
				result.append("#combine(");
			}

			for (Phrase p : requirements) {
				result.append(composePhrase(p));
				result.append(' ');
			}
			result.deleteCharAt(result.length() - 1);

			if (requirements.size() > 1) {
				result.append(")");
			}
		} else if (!rejections.isEmpty()) {
			// if everything is negation, so we have something in the query,
			// just prohibit the first element, given that we've already
			// #filrej'ed everything
			result.append("#not(");
			result.append(composePhrase(rejections.get(0)));
			result.append(')');
		} else {
			throw new DocSearchException("illegal query: empty");
		}

		// now we have the core; lets add the filters to the outside; since
		// requirements (#filereq) are likely to reduce the number of results
		// more quickly, make that the outer filter with the rejections within.

		// first wrap rejection filter if necessary
		if (!rejections.isEmpty()) {
			StringBuffer prefix = new StringBuffer("#filrej(");

			if (rejections.size() > 1) {
				prefix.append("#syn(");
			}

			for (Phrase p : rejections) {
				prefix.append(composePhrase(p));
				prefix.append(' ');
			}

			// delete last space
			prefix.deleteCharAt(prefix.length() - 1);

			if (rejections.size() > 1) {
				prefix.append(')');
			}

			prefix.append(' ');

			result.insert(0, prefix);
			result.append(")");
		}

		// now wrap requirements filter if necessary; this will be executed
		// before any rejection filter
		if (!requirements.isEmpty()) {
			StringBuffer prefix = new StringBuffer("#filreq(");

			if (requirements.size() > 1) {
				prefix.append("#band(");
			}

			for (Phrase p : requirements) {
				prefix.append(composePhrase(p));
				prefix.append(' ');
			}

			// delete last space
			prefix.deleteCharAt(prefix.length() - 1);

			if (requirements.size() > 1) {
				prefix.append(')');
			}

			prefix.append(' ');

			result.insert(0, prefix);
			result.append(")");
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("composeQuery produces: " + result);
		}
		return result;
	}
}
