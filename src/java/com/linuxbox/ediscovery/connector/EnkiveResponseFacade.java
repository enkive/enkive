package com.linuxbox.ediscovery.connector;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.ResponseStatus;

/**
 * The response data was being forced into a String in some cases, which would
 * corrupt binary data. So the EnkiveConnector now insures that the data is kept
 * in binary format, and this facade will convert it to a String on demand. That
 * way binary data (i.e., non-text data, data that is best stored as a
 * collection of bytes) can remain binary.
 * 
 * @author eric
 * 
 */
public class EnkiveResponseFacade extends Response {
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.ediscovery.connector");

	protected Response other;
	protected String data = null;
	protected String defaultEncoding;

	public EnkiveResponseFacade(Response other) {
		this(other, null);
	}

	public EnkiveResponseFacade(Response other, String defaultEncoding) {
		super(other.getStatus());
		this.other = other;
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Gets the response, converting it to a String on the fly if it has not
	 * already done so.
	 * 
	 * @return the data stream from the response object - will be null on error
	 *         or if the response has already been streamed to an OutputStream.
	 */
	public String getResponse() {
		if (data != null) {
			return data;
		}

		final String encoding = getEncoding();
		InputStream is = other.getResponseStream();

		if (is instanceof ByteArrayInputStream) {
			ByteArrayInputStream bis = (ByteArrayInputStream) is;
			final int available = bis.available();

			// read entire rest of input stream into bytes and return input
			// stream to former state
			bis.mark(available);
			final byte[] bytes = new byte[available];
			int count = 0;
			do {
				final int read = bis.read(bytes, count, available - count);
				count += read;
			} while (count < available);
			bis.reset();

			// try to build string of best possible encoding
			try {
				if (encoding != null) {
					data = new String(bytes, encoding);
				} else {
					data = (defaultEncoding != null ? new String(bytes,
							defaultEncoding) : new String(bytes));
				}
			} catch (UnsupportedEncodingException e) {
				LOGGER.error(
						"Could not convert response from Enkive web request to expected encoding.",
						e);
			}
		} else {
			LOGGER.error("Expected to have data that could be turned into a String but failed, but instead had "
					+ is.getClass().getName() + ".");
		}

		return data;
	}

	/**
	 * Gets the text of the response.
	 * 
	 * @return the text
	 */
	public String getText() {
		return this.getResponse();
	}

	/**
	 * Gets the response stream.
	 * 
	 * @return the response InputStream if set during construction, else will be
	 *         null.
	 */
	public InputStream getResponseStream() {
		return other.getResponseStream();
	}

	/**
	 * Gets the status.
	 * 
	 * @return Status object representing the response status and any error
	 *         information
	 */
	public ResponseStatus getStatus() {
		return other.getStatus();
	}

	@Override
	public String toString() {
		return this.getResponse();
	}
}
