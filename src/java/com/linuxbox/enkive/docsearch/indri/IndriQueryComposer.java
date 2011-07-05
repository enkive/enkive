package com.linuxbox.enkive.docsearch.indri;

import java.util.LinkedList;
import java.util.List;

import com.linuxbox.enkive.docsearch.TextQueryParser;
import com.linuxbox.enkive.docsearch.TextQueryParser.Phrase;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public class IndriQueryComposer {
	public static CharSequence composeQuery(String queryString)
			throws DocSearchException {
		return composeQuery(TextQueryParser.parseContentCriteria(queryString));
	}

	protected static CharSequence composePhrase(Phrase phrase) {
		StringBuffer result = new StringBuffer();

		if (phrase.hasMultipleTerms()) {
			result.append("#1(");
			result.append(phrase.getTermsAsCharSeq());
			result.append(")");
		} else {
			result.append(phrase.getTermsAsCharSeq());
		}

		return result;
	}

	/**
	 * Creates an Indri query by using the filter-require (#filreq) and
	 * filter-reject (#filrej) operators to filter the results. Then it uses the
	 * #compose operator to rank/weight the result of the the required elements.
	 * If there are no reuired elements, it uses a simple #not to complete the
	 * query.
	 * 
	 * @param query
	 * @return
	 * @throws DocSearchException
	 */
	public static CharSequence composeQuery(TextQueryParser.Query query)
			throws DocSearchException {
		StringBuffer result = new StringBuffer();

		/*
		 * Separate the requirements and the rejections.
		 */
		List<Phrase> requirements = new LinkedList<Phrase>();
		List<Phrase> rejections = new LinkedList<Phrase>();
		for (Phrase p : query) {
			if (p.isNegated()) {
				rejections.add(p);
			} else {
				requirements.add(p);
			}
		}

		if (!requirements.isEmpty()) {
			// prefer to combine requirements with #combine
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

		// add rejections filter if necessary
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

		// add requirements filter if necessary
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

		return result;
	}
}
