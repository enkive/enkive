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
 *******************************************************************************/
package com.linuxbox.enkive.testing.messageGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class SimpleRandomMessageGenerator extends AbstractMessageGenerator {

	protected Random randGen;
	protected String messageBodyDataDirectory;

	public SimpleRandomMessageGenerator(String messageBodyDataDirectory) {
		randGen = new Random();
		this.messageBodyDataDirectory = messageBodyDataDirectory;
	}

	@Override
	protected String generateMessageBody() {
		InputStream is = null;
		StringBuffer messageBody = new StringBuffer();
		try {
			File messageBodyDataDir = new File(messageBodyDataDirectory);
			File[] files = messageBodyDataDir.listFiles();

			int fileToGet = randGen.nextInt(files.length);
			is = new FileInputStream(files[fileToGet]);

			BufferedReader dis = new BufferedReader(new InputStreamReader(is));
			String s;
			while ((s = dis.readLine()) != null) {
				messageBody.append(s + System.getProperty("line.separator"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {

			}

		}
		return messageBody.toString();
	}

}
