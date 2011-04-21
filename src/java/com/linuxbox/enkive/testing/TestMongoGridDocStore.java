package com.linuxbox.enkive.testing;

import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.SimpleDocument;
import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;

public class TestMongoGridDocStore {
	static DocStoreService docService;
	static Set<String> indexSet = new HashSet<String>();

	private static void archive(String content) throws DocStoreException {
		String index = docService.store(new SimpleDocument(content,
				"text/plain", "txt"));
		indexSet.add(index);
		// System.out.println("index: " + index);
	}

	private static void archiveAll() {
		final String duplicated = "This is another test.";
		final String[] documents = { "This is a test.", duplicated,
				"This is a third test.", "This is a fourth test.", duplicated };

		for (String doc : documents) {
			try {
				archive(doc);
			} catch (DocStoreException e) {
				System.err.println(e);
			}
		}
	}

	private static void retrieveAll() {
		for (String index : indexSet) {
			System.out.print("retrieving " + index + ": ");
			try {
				Document d = docService.retrieve(index);
				String s = new String(d.getContentBytes());
				System.out.println(s);
			} catch (DocumentNotFoundException e) {
				System.out.println(e);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("Starting");

		try {
			docService = new MongoGridDocStoreService("enkive", "attachments");
			archiveAll();
			retrieveAll();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		System.out.println("Done");
	}
}
