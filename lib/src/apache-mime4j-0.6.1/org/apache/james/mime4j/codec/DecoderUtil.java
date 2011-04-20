/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mime4j.codec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.james.mime4j.util.CharsetUtil;

/**
 * Static methods for decoding strings, byte arrays and encoded words.
 */
public class DecoderUtil {
    private static Log log = LogFactory.getLog(DecoderUtil.class);

    private static final Pattern PATTERN_ENCODED_WORD = Pattern.compile(
            "(.*?)=\\?([^\\?]+?)\\?(\\w)\\?([^\\?]+?)\\?=", Pattern.DOTALL);

    /**
     * Decodes a string containing quoted-printable encoded data. 
     * 
     * @param s the string to decode.
     * @return the decoded bytes.
     */
    public static byte[] decodeQuotedPrintable(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            byte[] bytes = s.getBytes("US-ASCII");
            
            QuotedPrintableInputStream is = new QuotedPrintableInputStream(
                                               new ByteArrayInputStream(bytes));
            
            int b = 0;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException e) {
            // This should never happen!
            log.error(e);
            throw new IllegalStateException(e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decodes a string containing base64 encoded data. 
     * 
     * @param s the string to decode.
     * @return the decoded bytes.
     */
    public static byte[] decodeBase64(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            byte[] bytes = s.getBytes("US-ASCII");
            
            Base64InputStream is = new Base64InputStream(
                                        new ByteArrayInputStream(bytes));
            
            int b = 0;
            while ((b = is.read()) != -1) {
                baos.write(b);
            }
        } catch (IOException e) {
            // This should never happen!
            log.error(e);
            throw new IllegalStateException(e);
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Decodes an encoded text encoded with the 'B' encoding (described in 
     * RFC 2047) found in a header field body.
     * 
     * @param encodedText the encoded text to decode.
     * @param charset the Java charset to use.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't 
     *         supported.
     */
    public static String decodeB(String encodedText, String charset) 
            throws UnsupportedEncodingException {
        byte[] decodedBytes = decodeBase64(encodedText);
        return new String(decodedBytes, charset);
    }
    
    /**
     * Decodes an encoded text encoded with the 'Q' encoding (described in 
     * RFC 2047) found in a header field body.
     * 
     * @param encodedText the encoded text to decode.
     * @param charset the Java charset to use.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if the given Java charset isn't 
     *         supported.
     */
    public static String decodeQ(String encodedText, String charset)
            throws UnsupportedEncodingException {
        encodedText = replaceUnderscores(encodedText);
        
        byte[] decodedBytes = decodeQuotedPrintable(encodedText);
        return new String(decodedBytes, charset);
    }

    /**
     * Decodes a string containing encoded words as defined by RFC 2047. Encoded
     * words have the form =?charset?enc?encoded-text?= where enc is either 'Q'
     * or 'q' for quoted-printable and 'B' or 'b' for base64.
     * 
     * @param body the string to decode.
     * @return the decoded string.
     */
    public static String decodeEncodedWords(String body) {
        int tailIndex = 0;
        boolean lastMatchValid = false;

        StringBuilder sb = new StringBuilder();

        for (Matcher matcher = PATTERN_ENCODED_WORD.matcher(body); matcher.find();) {
            String separator = matcher.group(1);
            String mimeCharset = matcher.group(2);
            String encoding = matcher.group(3);
            String encodedText = matcher.group(4);

            String decoded = tryDecodeEncodedWord(mimeCharset, encoding, encodedText);
            if (decoded == null) {
                sb.append(matcher.group(0));
            } else {
                if (!lastMatchValid || !CharsetUtil.isWhitespace(separator)) {
                    sb.append(separator);
                }
                sb.append(decoded);
            }

            tailIndex = matcher.end();
            lastMatchValid = decoded != null;
        }

        if (tailIndex == 0) {
            return body;
        } else {
            sb.append(body.substring(tailIndex));
            return sb.toString();
        }
    }

    // return null on error
    private static String tryDecodeEncodedWord(final String mimeCharset,
            final String encoding, final String encodedText) {
        String charset = CharsetUtil.toJavaCharset(mimeCharset);
        if (charset == null) {
            if (log.isWarnEnabled()) {
                log.warn("MIME charset '" + mimeCharset + "' in encoded word '"
                        + recombine(mimeCharset, encoding, encodedText) + "' doesn't have a "
                        + "corresponding Java charset");
            }
            return null;
        } else if (!CharsetUtil.isDecodingSupported(charset)) {
            if (log.isWarnEnabled()) {
                log.warn("Current JDK doesn't support decoding of charset '"
                        + charset + "' (MIME charset '" + mimeCharset
                        + "' in encoded word '" + recombine(mimeCharset, encoding, encodedText)
                        + "')");
            }
            return null;
        }

        if (encodedText.length() == 0) {
            if (log.isWarnEnabled()) {
                log.warn("Missing encoded text in encoded word: '"
                        + recombine(mimeCharset, encoding, encodedText) + "'");
            }
            return null;
        }

        try {
            if (encoding.equalsIgnoreCase("Q")) {
                return DecoderUtil.decodeQ(encodedText, charset);
            } else if (encoding.equalsIgnoreCase("B")) {
                return DecoderUtil.decodeB(encodedText, charset);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Warning: Unknown encoding in encoded word '"
                            + recombine(mimeCharset, encoding, encodedText) + "'");
                }
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            // should not happen because of isDecodingSupported check above
            if (log.isWarnEnabled()) {
                log.warn("Unsupported encoding in encoded word '"
                        + recombine(mimeCharset, encoding, encodedText) + "'", e);
            }
            return null;
        } catch (RuntimeException e) {
            if (log.isWarnEnabled()) {
                log.warn("Could not decode encoded word '"
                        + recombine(mimeCharset, encoding, encodedText) + "'", e);
            }
            return null;
        }
    }

    private static String recombine(final String mimeCharset,
            final String encoding, final String encodedText) {
        return "=?" + mimeCharset + "?" + encoding + "?" + encodedText + "?=";
    }

    // Replace _ with =20
    private static String replaceUnderscores(String str) {
        // probably faster than String#replace(CharSequence, CharSequence)

        StringBuilder sb = new StringBuilder(128);

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                sb.append("=20");
            } else {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}
