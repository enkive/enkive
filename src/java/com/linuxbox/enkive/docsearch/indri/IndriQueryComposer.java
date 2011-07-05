package com.linuxbox.enkive.docsearch.indri;

import com.linuxbox.enkive.docsearch.TextQueryParser;
import com.linuxbox.enkive.docsearch.TextQueryParser.Phrase;

public class IndriQueryComposer {
	public static CharSequence composeQuery(String queryString) {
		return composeQuery(TextQueryParser.parseContentCriteria(queryString));
	}

	public static CharSequence composeQuery(TextQueryParser.Query query) {
		StringBuffer result = new StringBuffer();

		result.append("#band<");

		for (Phrase p : query) {
			if (p.isNegated()) {
				result.append("#not<");
			}

			if (p.hasMultipleTerms()) {
				result.append("#1<");
				result.append(p.getTermsAsCharSeq());
				result.append(">");
			} else {
				result.append(p.getTermsAsCharSeq());
			}

			if (p.isNegated()) {
				result.append(">");
			}
			
			result.append(' ');
		}
		result.deleteCharAt(result.length() - 1);

		result.append(">");

		return result;
	}
}
