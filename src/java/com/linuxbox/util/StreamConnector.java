package com.linuxbox.util;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class StreamConnector {
	private final static int BUFFER_SIZE_DEFAULT = 4096;

	/**
	 * Asynchronously transfers data from an InputStream to an OutputStream
	 * until the end of the InputStream is reached. Returns a
	 * StreamConnectorStatus that can be queried to note the progress and
	 * problems of the asynchronous transfer. The InputStream is closed when its
	 * end is reached.
	 * 
	 * @param input
	 *            the InputStream
	 * @param output
	 *            the OutputStream
	 * @return a StreamConnectorStatus that can be queried to determine the
	 *         progress and error-state of the transfer
	 */
	public static StreamConnectorStatus transferBackground(InputStream input,
			OutputStream output) {
		StreamConnectorStatusImpl result = new StreamConnectorStatusImpl(input,
				output, BUFFER_SIZE_DEFAULT);
		result.start();
		return result;
	}

	/**
	 * Transfers data from an InputStream to an OutputStream until the end of
	 * the InputStream is reached. The InputStream is closed when its end is
	 * reached.
	 * 
	 * @param input
	 *            the InputStream
	 * @param output
	 *            the OutputStream
	 * @throws IOException
	 */
	public static void transferForeground(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE_DEFAULT];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		input.close();
	}

	/**
	 * Transfers characters from a Reader to a Writer until the end of the
	 * character stream is reached. The Reader is closed when its end is
	 * reached.
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void transferForeground(Reader input, Writer output)
			throws IOException {
		char[] buffer = new char[BUFFER_SIZE_DEFAULT];
		int charsRead;
		while ((charsRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, charsRead);
		}
		input.close();
	}

	static class StreamConnectorStatusImpl extends Thread implements
			StreamConnectorStatus {
		private InputStream input;
		private OutputStream output;
		private int bufferSize;

		private boolean finished;
		private int bytesTransferred;
		private IOException ioException;

		StreamConnectorStatusImpl(InputStream input, OutputStream output,
				int bufferSize) {
			this.input = input;
			this.output = output;
			this.bufferSize = bufferSize;
			finished = false;
			bytesTransferred = 0;
			ioException = null;
		}

		public int getBytesTransferred() {
			return bytesTransferred;
		}

		public boolean isFinished() throws IOException {
			if (ioException != null) {
				throw ioException;
			}
			return finished;
		}

		public void waitToFinish() throws InterruptedException {
			this.join();
		}

		public void run() {
			try {
				byte[] buffer = new byte[bufferSize];
				int bytesRead;
				while ((bytesRead = input.read(buffer)) != -1) {
					output.write(buffer, 0, bytesRead);
					bytesTransferred += bytesRead;
				}
				input.close();
			} catch (IOException e) {
				ioException = e;
			} finally {
				finished = true;
			}
		}
	}
}
