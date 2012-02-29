/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TextQueryParser {
	public static class Phrase {
		List<CharSequence> terms;
		boolean negated;

		public Phrase(boolean negated) {
			this.negated = negated;
			terms = new ArrayList<CharSequence>();
		}

		public Phrase() {
			this(false);
		}

		public Phrase(String phrase, boolean negated) {
			this(negated);
			terms.add(phrase);
		}

		public Phrase(String phrase) {
			this(phrase, false);
		}

		public void addTerm(String term) {
			terms.add(term);
		}

		public boolean hasMultipleTerms() {
			return terms.size() > 1;
		}

		public boolean anyTerms() {
			return terms.size() > 0;
		}

		public boolean isNegated() {
			return negated;
		}

		public void setNegated(boolean negated) {
			this.negated = negated;
		}

		public CharSequence getTermsAsCharSeq() {
			StringBuffer result = new StringBuffer();

			for (CharSequence s : terms) {
				result.append(s);
				result.append(' ');
			}

			// delete last space
			result.deleteCharAt(result.length() - 1);

			return result;
		}

		public ListIterator<CharSequence> getTermsListIterator() {
			return terms.listIterator();
		}
	}

	public static class Query implements Iterable<Phrase> {
		Collection<Phrase> phrases;

		public Query() {
			phrases = new LinkedList<Phrase>();
		}

		public void addPhrase(Phrase phrase) {
			phrases.add(phrase);
		}

		@Override
		public Iterator<Phrase> iterator() {
			return phrases.iterator();
		}
	}

	/**
	 * Parse a criteria as a single string into a collection (list) of
	 * criterion. Double quotes define phrases, provided the initial quote
	 * occurs at the start of a word and the ending quote is at the end of a
	 * word. Also a minus sign / hyphen (-) defines negation. It must occur at
	 * the start of a word NOT in a phrase. Or it must occur before the opening
	 * double quote for a phrase.
	 * 
	 * Hyphens within words and double quotes not in the appropriate place have
	 * no effect and may yield search results not as the user intended. Perhaps
	 * this should error out in those cases?
	 * 
	 * @param criteriaString
	 * @return
	 */
	public static Query parseContentCriteria(String criteriaString) {
		Query resultQuery = new Query();
		// convert sequences of whitespace to a single space; remove whitespace
		// at start and end
		criteriaString = criteriaString.replaceAll("\\s+", " ").trim();
		String[] criteriaList = criteriaString.split(" ");

		Phrase currentPhrase = null;

		for (String criterion : criteriaList) {
			StringBuffer mutableCriterion = new StringBuffer(criterion);

			// if not currently within a phrase
			if (currentPhrase == null) {
				currentPhrase = new Phrase();

				if (mutableCriterion.indexOf("-") == 0) {
					currentPhrase.setNegated(true);
					mutableCriterion.deleteCharAt(0);
				}

				// if we have an initial double quote, we're starting a phrase
				if (mutableCriterion.indexOf("\"") == 0) {
					mutableCriterion.deleteCharAt(0);
				} else {
					// otherwise we just have a word
					if (criterion.length() > 0) {
						currentPhrase.addTerm(mutableCriterion.toString());
						resultQuery.addPhrase(currentPhrase);
						currentPhrase = null;
					}
				}
			}

			// if we are currently in a phrase... ; note: if the phrase consists
			// of one word we will execute the previous conditional *and* this
			// one
			if (currentPhrase != null) {
				// add a space between words in phrase

				// check for phrase ending
				final int lastQuoteIndex = mutableCriterion.lastIndexOf("\"");
				if (lastQuoteIndex >= 0
						&& lastQuoteIndex == mutableCriterion.length() - 1) {
					mutableCriterion
							.deleteCharAt(mutableCriterion.length() - 1);
					if (mutableCriterion.length() > 0) {
						currentPhrase.addTerm(mutableCriterion.toString());
					}

					if (currentPhrase.anyTerms()) {
						resultQuery.addPhrase(currentPhrase);
					}
					currentPhrase = null;
				} else {
					if (mutableCriterion.length() > 0) {
						currentPhrase.addTerm(mutableCriterion.toString());
					}
				}
			}
		} // for loop

		// see if there are any non-closed phrases and add them on as well
		if (currentPhrase != null && currentPhrase.anyTerms()) {
			resultQuery.addPhrase(currentPhrase);
		}

		return resultQuery;
	}

	public static String unparseContentCriteria(Query query) {
		StringBuffer result = new StringBuffer();

		for (Phrase p : query) {
			if (p.negated) {
				result.append('-');
			}

			if (p.hasMultipleTerms()) {
				result.append('"');
				result.append(p.getTermsAsCharSeq());
				result.append('"');
			} else {
				result.append(p.getTermsAsCharSeq());
			}

			result.append(' ');
		}

		// remove last space
		result.deleteCharAt(result.length() - 1);

		return result.toString();
	}
}
