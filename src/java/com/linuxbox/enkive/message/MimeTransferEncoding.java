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
package com.linuxbox.enkive.message;

import java.util.HashMap;
import java.util.Map;

public enum MimeTransferEncoding {
	SEVEN_BIT("7bit"), QUOTED_PRINTABLE("quoted-printable"), BASE64("base64"), EIGHT_BIT(
			"8bit"), BINARY("binary");

	/**
	 * A map that associates the String name of the encoding with the encoding
	 * itself.
	 */
	private static Map<String, MimeTransferEncoding> map;

	/**
	 * The String equivalent for the encoding.
	 */
	private String string;

	MimeTransferEncoding(String string) {
		this.string = string;
	}

	/**
	 * @return the String equivalent of an encoding.
	 */
	public String toString() {
		return string;
	}

	/**
	 * Given a String it returns the corresponding MimeTransferEncoding for that
	 * String, or null if there isn't a known encoding for that String. The
	 * String would come from the Content-Transfer-Encoding header.
	 * 
	 * @param s
	 *            A String containing a MIME transfer encoding.
	 * @return One of the enumerated values or null.
	 */
	public static MimeTransferEncoding parseString(String s) {
		// Initially I tried to fill the map in the constructor, but I got a
		// compiler error about not being able to access a static variable from
		// within an "initializer" (apparently referring to the constructor).
		if (map == null) {
			initializeMap();
		}

		return map.get(s);
	}

	/**
	 * Adds an entry in the map for each encoding, keyed by the String
	 * descripter. The map can then be used to look up the encoding from the
	 * string descriptor.
	 */
	private static void initializeMap() {
		map = new HashMap<String, MimeTransferEncoding>();
		for (MimeTransferEncoding mte : MimeTransferEncoding.values()) {
			map.put(mte.string, mte);
		}
	}

	/*
	 * A little code to test out this enum.
	 */
	public static void main(String[] args) {
		MimeTransferEncoding[] mtes = {
				MimeTransferEncoding.parseString("7bit"),
				MimeTransferEncoding.parseString("base64"),
				MimeTransferEncoding.parseString("bad") };

		for (MimeTransferEncoding mte : mtes) {
			if (mte == null) {
				System.out.println("no encoding");
			} else {
				System.out.println("have a " + mte);
			}
		}
	}
}
