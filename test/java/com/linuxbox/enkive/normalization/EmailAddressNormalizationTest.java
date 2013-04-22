package com.linuxbox.enkive.normalization;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class EmailAddressNormalizationTest {
	@Test
	public void testCaseEmailAddressNormalizer() {
		EmailAddressNormalizer n1 = new CaseEmailAddressNormalizer();

		assertEquals("all lower case", "elizabethzephyr@gmail.com",
				n1.map("elizabethzephyr@gmail.com"));
		assertEquals("some capitals in local part",
				"elizabethzephyr@gmail.com",
				n1.map("ElizabethZephyr@gmail.com"));
		assertEquals("some capitals in domain part",
				"elizabethzephyr@gmail.com",
				n1.map("elizabethzephyr@Gmail.com"));
		assertEquals("some capitals in both local and domain parts",
				"elizabethzephyr@gmail.com",
				n1.map("ElizabethZephyr@Gmail.coM"));
		assertEquals("all upper case", "elizabethzephyr@gmail.com",
				n1.map("ELIZABETHZEPHYR@GMAIL.COM"));

		CaseEmailAddressNormalizer n2 = new CaseEmailAddressNormalizer();
		n2.setCaseFoldLocalPart(false);

		assertEquals("all lower case", "elizabethzephyr@gmail.com",
				n2.map("elizabethzephyr@gmail.com"));
		assertEquals("some capitals in local part",
				"ElizabethZephyr@gmail.com",
				n2.map("ElizabethZephyr@gmail.com"));
		assertEquals("some capitals in domain part",
				"elizabethzephyr@gmail.com",
				n2.map("elizabethzephyr@Gmail.com"));
		assertEquals("some capitals in both local and domain parts",
				"ElizabethZephyr@gmail.com",
				n2.map("ElizabethZephyr@Gmail.coM"));
		assertEquals("all upper case", "ELIZABETHZEPHYR@gmail.com",
				n2.map("ELIZABETHZEPHYR@GMAIL.COM"));
	}

	@Test
	public void testLocalPartDotAddressNormalizer() {
		EmailAddressNormalizer n = new LocalDotsEmailAddressNormalizer();

		assertEquals("test single dot", "elizabethzephyr@gmail.com",
				n.map("elizabeth.zephyr@gmail.com"));
		assertEquals("test multiple dots", "dashdashdashdashdash@gmail.com",
				n.map("dash.dash.dash.dash..dash@gmail.com"));
		assertEquals("test dots at start and end of local part",
				"surround@gmail.com", n.map("...surround...@gmail.com"));
	}

	@Test
	public void testTaggedEmailAddressNormalizer() {
		TaggedEmailAddressNormalizer n1 = new TaggedEmailAddressNormalizer();
		n1.setTagDelimiter('+');

		assertEquals("no tag delimiters", "realaddress@example.com",
				n1.map("realaddress@example.com"));
		assertEquals("one tag delimiter", "realaddress@example.com",
				n1.map("realaddress+sometag@example.com"));
		assertEquals("two tag delimiters", "realaddress@example.com",
				n1.map("realaddress+some+tag@example.com"));
		assertEquals("tag delimiter in domain part but not local part",
				"realaddress@example+foo.com",
				n1.map("realaddress@example+foo.com"));

		TaggedEmailAddressNormalizer n2 = new TaggedEmailAddressNormalizer();
		n2.setTagDelimiter('-');

		assertEquals("one non-matching tag delimiter",
				"realaddress+sometag@example.com",
				n2.map("realaddress+sometag@example.com"));
		assertEquals("two non-matching tag delimiters",
				"realaddress+some+tag@example.com",
				n2.map("realaddress+some+tag@example.com"));
		assertEquals(
				"one non-matching tag delimiter in domain part but not local part",
				"realaddress@example+foo.com",
				n2.map("realaddress@example+foo.com"));

		assertEquals("no tag delimiters", "realaddress@example.com",
				n2.map("realaddress@example.com"));
		assertEquals("one tag delimiter", "realaddress@example.com",
				n2.map("realaddress-sometag@example.com"));
		assertEquals("two tag delimiters", "realaddress@example.com",
				n2.map("realaddress-some-tag@example.com"));
		assertEquals("tag delimiter in domain part but not local part",
				"realaddress@example-foo.com",
				n2.map("realaddress@example-foo.com"));
	}

	@Test
	public void testSequenceEmailAddressNormalizer() {
		SequenceEmailAddressNormalizer n = new SequenceEmailAddressNormalizer();

		ArrayList<EmailAddressNormalizer> list = new ArrayList<EmailAddressNormalizer>();
		list.add(new CaseEmailAddressNormalizer());
		list.add(new LocalDotsEmailAddressNormalizer());
		{
			TaggedEmailAddressNormalizer tn = new TaggedEmailAddressNormalizer();
			tn.setTagDelimiter('+');
			list.add(tn);
		}
		n.setNormalizerList(list);

		assertEquals("test address w/ mixed case, dots, and a tag",
				"elizabethzephyr@gmail.com",
				n.map("Elizabeth.Zephyr+mytag@GMail.com"));
	}
}
