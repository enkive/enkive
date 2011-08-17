/*
 *  Copyright 2011 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.mailprocessor.processors;

import static com.linuxbox.enkive.mailprocessor.MailDirConstants.END_OF_STREAM_INDICATOR;
import static com.linuxbox.enkive.mailprocessor.MailDirConstants.HAS_END_OF_STREAM_REPLACEMENT_REGEX;
import static com.linuxbox.enkive.mailprocessor.ProcessorState.WAITING_FOR_DATA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.MailboxList;
import org.apache.james.mime4j.field.address.parser.ParseException;

import com.linuxbox.enkive.exception.SocketClosedException;
import com.linuxbox.enkive.mailprocessor.AbstractMailProcessor;
import com.linuxbox.enkive.mailprocessor.ProcessorState;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.Utility;

public class MailDirProcessor extends AbstractMailProcessor {
	// This is always disabled by default
	// More logic may need to be worked into this system
	private static boolean INFER_MAIL_FROM_AND_RCPT_TO = false;

	private BufferedReader reader;

	String mailFrom = "";
	ArrayList<String> rcptTo;

	@Override
	protected void prepareProcessor() throws UnknownHostException, IOException {
		this.multiMessage = true;
		reader = new BufferedReader(new InputStreamReader(socket
				.getInputStream()));
	}

	/**
	 * Reads each line from a single message and returns the full message. There
	 * is a line with a single "." between messages. If a line that's part of a
	 * message has a sequence of one or more "."s then it gets one "." added and
	 * then we remove it here.
	 * 
	 * Unfortunately this is confusing method. It returns some values via
	 * instance variables (e.g., mailFrom, rcptTo, processingComplete) and the
	 * text of the message as a String. TODO This needs to be re-factored and
	 * cleaned up.
	 */
	@Override
	protected String processInput() throws MessageIncompleteException,
			SocketClosedException, IOException {
		// Reset reused variables
		mailFrom = "";
		rcptTo = new ArrayList<String>();

		int character;
		ArrayList<String> addresses = new ArrayList<String>();
		String address = "";

		processorState = WAITING_FOR_DATA;
		do {
			character = reader.read();

			// check for closed socket / end-of-stream
			if (character < 0) {
				if (!addresses.isEmpty() || !address.isEmpty()) {
					logger
							.error("MailDirProcessor: socket closed after reading address data");
					throw new MessageIncompleteException(
							"socket closed while reading address line");
				}

				this.processingComplete = true;
				throw new SocketClosedException();
			}

			processorState = ProcessorState.READING_ADDRESSES;

			// check for delimeters
			if (character == ';' || character == ' ') {
				if (!address.isEmpty()) {
					addresses.add(address);
					address = "";
				}
			} else {
				address += character;
			}
		} while (character != ';');

		if (!addresses.isEmpty()) {
			mailFrom = addresses.remove(0);
			rcptTo = addresses;
		}

		processorState = ProcessorState.READING_MESSAGE;
		StringBuilder m = new StringBuilder();
		String tmp = reader.readLine();
		while (tmp != null && !tmp.equals(END_OF_STREAM_INDICATOR)) {
			if (HAS_END_OF_STREAM_REPLACEMENT_REGEX.matcher(tmp.trim())
					.matches()) {
				int index = tmp.indexOf(END_OF_STREAM_INDICATOR);
				// remove one "."
				tmp = tmp.substring(0, index) + tmp.substring(index + 1);
			}
			m.append(tmp + "\r\n");
			tmp = reader.readLine();
		}

		if (tmp == null) {
			throw new MessageIncompleteException(
					"socket closed before end-of-message indicator", m
							.toString());
		}

		return m.toString();
	}

	/**
	 * Parses out a single address if possible. If it doesn't find exactly one
	 * address it logs the warning but continues as best it can. If there are no
	 * addresses, null is returned.
	 * 
	 * @param fullAddress
	 * @param headerTypeDescription
	 *            -- a string describing which type of header it is; only used
	 *            for logging and error messages
	 * @return
	 */
	private String parseOutAddress(String fullAddress,
			String headerTypeDescription) {
		String strippedAddress = Utility
				.stripBracketsFromFromAddress(fullAddress);

		try {
			MailboxList mailboxes = AddressList.parse(strippedAddress)
					.flatten();

			// log anything unexpected
			if (mailboxes.size() != 1) {
				logger.error("Email address header " + headerTypeDescription
						+ "(\"" + strippedAddress
						+ "\") could not be parsed as a single email address.");
			}

			// return something reasonable if possible; null otherwise
			if (mailboxes.size() >= 1) {
				// TODO should we be adding the angle brackets around the
				// address?
				return mailboxes.get(0).getAddress();
			} else {
				return null;
			}
		} catch (ParseException e) {
			logger
					.error("Email address header "
							+ headerTypeDescription
							+ "(\""
							+ strippedAddress
							+ "\") could not be parsed as one or more email addresses.");
			return null;
		}
	}

	@Override
	protected Message postProcess(Message message) {
		if (INFER_MAIL_FROM_AND_RCPT_TO) {
			String address = parseOutAddress(message.getFrom(), "FROM");
			if (address != null) {
				message.setMailFrom(address);
			} else {
				logger.warn("did not get a FROM address from "
						+ message.getFrom());
			}

			for (String unparsedAddress : message.getTo()) {
				String parsedAddress = parseOutAddress(unparsedAddress, "TO");
				if (parsedAddress != null) {
					message.appendRcptTo(parsedAddress);
				}
			}

			for (String unparsedAddress : message.getCc()) {
				String parsedAddress = parseOutAddress(unparsedAddress, "CC");
				if (parsedAddress != null) {
					message.appendRcptTo(parsedAddress);
				}
			}

			// NOTE: we cannot add BCCs, since BCCs are addresses in RCPT_TO but
			// not explicitly in TO or CC
		} else {
			message.setMailFrom(mailFrom);
			message.setRcptTo(rcptTo);
		}
		return message;
	}

	@Override
	protected void closeProcessor() {
		// empty
	}
}
