/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.workspace.searchFolder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public abstract class SearchFolder {

	protected String ID;
	protected Collection<SearchFolderSearchResult> results;
	protected SearchFolderSearchResultBuilder searchResultBuilder;
	protected MessageRetrieverService retrieverService;

	public SearchFolder(SearchFolderSearchResultBuilder searchResultBuilder) {
		results = new HashSet<SearchFolderSearchResult>();
		this.searchResultBuilder = searchResultBuilder;
	}

	public String getID() {
		return ID;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void addSearchResult(SearchResult searchResult)
			throws WorkspaceException {
		SearchFolderSearchResult searchFolderSearchResult = searchResultBuilder
				.buildSearchResult(searchResult);
		searchFolderSearchResult.saveSearchResult();
		results.add(searchFolderSearchResult);

	}

	public Map<Long, String> getMessageIds() {
		Map<Long, String> messageIds = new HashMap<Long, String>();
		for (SearchFolderSearchResult result : results)
			messageIds.putAll(result.getMessageIds());
		return messageIds;
	}

	public Map<Long, String> getMessageIds(String sortField, int sortDir) {
		Map<Long, String> messageIds = new HashMap<Long, String>();
		for (SearchFolderSearchResult result : results)
			messageIds.putAll(result.getMessageIds());
		return messageIds;
	}

	public void removeMessageId(String messageId) throws WorkspaceException {
		throw new WorkspaceException("Unimplemented");
	}

	public void removeMessageIds(Collection<String> messageIds)
			throws WorkspaceException {
		throw new WorkspaceException("Unimplemented");
	}

	/**
	 * Writes a tar.gz file to the provided outputstream
	 * 
	 * @param outputStream
	 * @throws IOException
	 */
	public void exportSearchFolder(OutputStream outputStream)
			throws IOException {
		BufferedOutputStream out = new BufferedOutputStream(outputStream);
		GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out);
		TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut);

		File mboxFile = File.createTempFile("mbox-export", ".mbox");
		BufferedWriter mboxWriter = new BufferedWriter(new FileWriter(mboxFile));
		// Write mbox to tempfile?
		for (String messageId : getMessageIds().values()) {
			try {
				Message message = retrieverService.retrieve(messageId);

				mboxWriter.write("From " + message.getDateStr() + "\r\n");
				BufferedReader reader = new BufferedReader(new StringReader(
						message.getReconstitutedEmail()));
				String tmpLine;
				while ((tmpLine = reader.readLine()) != null) {
					if (tmpLine.startsWith("From "))
						mboxWriter.write(">" + tmpLine);
					else
						mboxWriter.write(tmpLine);
					mboxWriter.write("\r\n");
				}
			} catch (CannotRetrieveException e) {
				// Add errors to report
				// if (LOGGER.isErrorEnabled())
				// LOGGER.error("Could not retrieve message with id"
				// + messageId);
			}
		}
		mboxWriter.flush();
		mboxWriter.close();
		// Add mbox to tarfile
		TarArchiveEntry mboxEntry = new TarArchiveEntry(mboxFile,
				"filename.mbox");
		tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
		tOut.putArchiveEntry(mboxEntry);
		IOUtils.copy(new FileInputStream(mboxFile), tOut);
		tOut.flush();
		tOut.closeArchiveEntry();
		mboxWriter.close();
		mboxFile.delete();
		// Create report in tempfile?

		// Add report to tarfile

		// Close out stream
		tOut.finish();
		outputStream.flush();
		tOut.close();
		outputStream.close();

	}

	public MessageRetrieverService getRetrieverService() {
		return retrieverService;
	}

	public void setRetrieverService(MessageRetrieverService retrieverService) {
		this.retrieverService = retrieverService;
	}

	public abstract void saveSearchFolder();

}
