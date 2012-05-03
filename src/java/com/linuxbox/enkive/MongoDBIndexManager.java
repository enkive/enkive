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
 ******************************************************************************/

package com.linuxbox.enkive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.util.lockservice.LockService;
import com.linuxbox.util.mongodb.MongoIndexable;
import com.linuxbox.util.mongodb.MongoIndexable.IndexDescription;
import com.linuxbox.util.queueservice.QueueService;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class MongoDBIndexManager {
	/**
	 * The set of services that might have MongoDB indices.
	 */
	static final Service[] SERVICES = {
			new Service("DocLockService", LockService.class),
			new Service("AuditLogService", AuditService.class),
			new Service("DocStoreService", DocStoreService.class),
			new Service("IndexerQueueService", QueueService.class), };

	static final String[] CONFIG_FILES = { "enkive-server.xml" };

	static final BufferedReader in = new BufferedReader(new InputStreamReader(
			System.in));

	/**
	 * Thrown when the user has decided to quit.
	 * 
	 * @author eric
	 * 
	 */
	static class QuitException extends Exception {
		private static final long serialVersionUID = 6931399708421895460L;
		// empty
	}

	static class Service {
		String name;
		Class<?> klass;

		public Service(String name, Class<?> klass) {
			this.name = name;
			this.klass = klass;
		}
	}

	public static void main(String[] args) {
		final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				CONFIG_FILES);
		context.registerShutdownHook();

		try {
			for (Service service : SERVICES) {
				final Object theService = context.getBean(service.name,
						service.klass);
				checkService(service.name, theService);
			}

			System.out.println("Done; all indexes checked.");
		} catch (QuitException e) {
			System.out.println("Quitting at user request.");
		}

		context.close();
	}

	public static boolean indexesMatch(IndexDescription pref, DBObject actual) {
		DBObject actualKey = (DBObject) actual.get("key");
		return pref.getDescription().equals(actualKey);
	}

	public static void ensureIndex(MongoIndexable service,
			IndexDescription desiredIndex, boolean inBackground)
			throws MongoException {
		final DBObject options = BasicDBObjectBuilder.start()
				.add("name", desiredIndex.getName())
				.add("unique", desiredIndex.isUnique())
				.add("background", inBackground).get();
		service.ensureIndex(desiredIndex.getDescription(), options);
	}

	public static void ensureIndexes(MongoIndexable service,
			List<IndexDescription> desiredIndexes, boolean inBackground)
			throws MongoException {
		for (IndexDescription index : desiredIndexes) {
			ensureIndex(service, index, inBackground);
		}
	}

	public static void matchIndexes(MongoIndexable service,
			List<IndexDescription> preferredIndexes,
			List<DBObject> actualIndexes) throws QuitException {
		/*
		 * Whether to give user opportunity to create index when one isn't
		 * found.
		 */
		boolean reportOnly = false;

		preferred: for (IndexDescription pref : preferredIndexes) {
			for (DBObject actual : actualIndexes) {
				if (indexesMatch(pref, actual)) {
					System.out.println("Index \"" + pref.getName()
							+ "\" exists.");
					continue preferred;
				}
			}

			try {
				while (true) {
					System.out.println("Index \"" + pref.getName()
							+ "\" does not appear to exist.");
					if (reportOnly) {
						break;
					}

					System.out.println("Options:");
					System.out.println("    (c)reate index in the background");
					System.out.println("    (c!)reate index in the foreground");
					System.out.println("    (s)kip this index");
					System.out
							.println("    (r)eport on other indexes without further prompts");
					System.out.println("    (q)uit this program");
					System.out.print("Your choice: ");

					String input = in.readLine();

					if (input.equalsIgnoreCase("c")) {
						ensureIndex(service, pref, true);
						break;
					} else if (input.equalsIgnoreCase("c!")) {
						ensureIndex(service, pref, false);
						break;
					} else if (input.equalsIgnoreCase("s")) {
						break;
					} else if (input.equalsIgnoreCase("r")) {
						reportOnly = true;
						break;
					} else if (input.equalsIgnoreCase("q")) {
						throw new QuitException();
					} else {
						System.out.println("I do not understand \"" + input
								+ "\".");
					}
				}
			} catch (IOException e) {
				System.err.println("Error: " + e.getLocalizedMessage());
				System.err
						.println("Switching into report-only mode for remainder of run.");
				reportOnly = true;
			} catch (MongoException e) {
				System.err.println("Error: " + e.getLocalizedMessage());
				System.err
						.println("Switching into report-only mode for remainder of run.");
				reportOnly = true;
			}
		}
	}

	public static void checkService(String name, Object service)
			throws QuitException {
		if (!(service instanceof MongoIndexable)) {
			System.out.println(name + " is not a MongoDB indexable service.");
			return;
		}

		System.out.println("Checking indexes for " + name + "....");

		MongoIndexable indexable = (MongoIndexable) service;

		List<IndexDescription> preferredIndexes = indexable
				.getPreferredIndexes();
		List<DBObject> actualIndexes = indexable.getIndexInfo();

		matchIndexes(indexable, preferredIndexes, actualIndexes);
	}

	/*
	 * This function contains some experiments to determine whether it would be
	 * able to get the key information about a service without actually
	 * instantiating it. That way we could potentially look up the database and
	 * collection that a given service used and talk to MongoDB directly without
	 * every using the overhead to instantiate the actual service.
	 */
	@SuppressWarnings("unused")
	private static void unusedExperiments() {
		XmlBeanFactory beanFactory = new XmlBeanFactory(new ClassPathResource(
				"enkive-server.xml"));
		// XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
		// beanFactory);
		// beanReader.loadBeanDefinitions("enkive-server.xml");
		final int count = beanFactory.getBeanDefinitionCount();
		System.out.println("Found " + count + " beans.");
		final String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String s : beanNames) {
			System.out.println(s);
		}

		// beanFactory.preInstantiateSingletons();

		BeanDefinition def = beanFactory.getBeanDefinition("DocLockService");

		System.out.println(def.getAttribute("server"));
		// System.out.println(def.getAttribute("port"));
		// System.out.println(def.getAttribute("database"));
		// System.out.println(def.getAttribute("collection"));

		for (String an : def.attributeNames()) {
			System.out.println("attribute name: " + an);
		}

		ConstructorArgumentValues cavs = def.getConstructorArgumentValues();
		System.out.println("constructor has " + cavs.getArgumentCount()
				+ " arguments");
		Map<Integer, ValueHolder> m = cavs.getIndexedArgumentValues();
		System.out.println("indexed arguments of length " + m.size());
		List<ValueHolder> l = cavs.getGenericArgumentValues();
		System.out.println("generic arguments of length " + l.size());

		int count2 = 0;
		for (ValueHolder vh : l) {
			System.out.println(count2);
			System.out.println("    " + vh.getName());
			System.out.println("    " + vh.getType());
			System.out.println("    " + vh.getSource());
			System.out.println("    " + vh.getValue());
			System.out.println("    " + vh.getConvertedValue());
			++count2;
		}

		System.out.println("done");
	}
}
