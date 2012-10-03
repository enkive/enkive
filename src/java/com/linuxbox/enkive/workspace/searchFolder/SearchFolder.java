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
import java.util.HashSet;
import java.util.Set;

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

	public Collection<String> getMessageIds() {
		Collection<String> messageIds = new HashSet<String>();
		for (SearchFolderSearchResult result : results)
			messageIds.addAll(result.getMessageIds());
		return messageIds;
	}

	public Collection<String> getMessageIds(String sortField, int sortDir) {
		Collection<String> messageIds = new HashSet<String>();
		for (SearchFolderSearchResult result : results)
			messageIds.addAll(result.getMessageIds());
		return messageIds;
	}

	public void removeMessageId(String messageId) throws WorkspaceException {
		for (SearchFolderSearchResult result : results) {
			Set<String> folderMessageIds = result.getMessageIds();
			if (folderMessageIds.remove(messageId)) {
				if (folderMessageIds.isEmpty()) {
					result.deleteSearchResult();
					results.remove(result);
				} else {
					result.setMessageIds(folderMessageIds);
					result.saveSearchResult();
				}
			}
		}
	}

	public void removeMessageIds(Collection<String> messageIds)
			throws WorkspaceException {
		for (SearchFolderSearchResult result : results) {
			Set<String> folderMessageIds = result.getMessageIds();
			if (folderMessageIds.removeAll(messageIds)) {
				if (folderMessageIds.isEmpty()) {
					result.deleteSearchResult();
					results.remove(result);
				} else {
					result.setMessageIds(folderMessageIds);
					result.saveSearchResult();
				}
			}
		}
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
		for (String messageId : getMessageIds()) {
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
