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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive");

	/**
	 * The set of services that might have MongoDB indices.
	 */
	static final Service[] SERVICES = {
			new Service("DocLockService", LockService.class),
			new Service("AuditLogService", AuditService.class),
			new Service("DocStoreService", DocStoreService.class),
			new Service("IndexerQueueService", QueueService.class), };

	/**
	 * The xml file to use if we need to build our own beans.
	 */
	static final String[] CONFIG_FILES = { "enkive-server.xml" };

	/**
	 * How many documents are allowed in a collection if we automatically ensure
	 * that the index exists.
	 */
	static public final long MAX_DOCS_FOR_AUTO_ENSURE_INDEX = 100;

	/**
	 * The application context that we're using.
	 */
	AbstractApplicationContext context;

	/**
	 * Basic constructor.
	 * 
	 * @param context
	 */
	public MongoDBIndexManager(AbstractApplicationContext context) {
		this.context = context;
	}

	/**
	 * Thrown when the user has decided to quit.
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

	interface IndexActions {
		public void notMongoIndexable(String name);

		public void checkingIntro(String name);

		public void hasIndex(IndexDescription pref);

		public void doesNotHaveIndex(MongoIndexable service,
				String serviceName, IndexDescription pref) throws QuitException;
	}

	class ConsoleIndexActions implements IndexActions {
		BufferedReader in;
		PrintStream out;
		PrintStream err;
		boolean reportOnly = false;

		public ConsoleIndexActions(InputStream input, PrintStream output,
				PrintStream error) {
			this.in = new BufferedReader(new InputStreamReader(input));
			this.out = output;
			this.err = error;
		}

		@Override
		public void notMongoIndexable(String name) {
			System.out.println(name + " is not a MongoDB indexable service.");
		}

		@Override
		public void checkingIntro(String name) {
			System.out.println("Checking indexes for " + name + "....");
		}

		@Override
		public void hasIndex(IndexDescription pref) {
			System.out.println("    Index \"" + pref.getName() + "\" exists.");

		}

		@Override
		public void doesNotHaveIndex(MongoIndexable service,
				String serviceName, IndexDescription pref) throws QuitException {
			try {
				while (true) {
					out.println("*** Index \"" + pref.getName()
							+ "\" does not appear to exist.");
					if (reportOnly) {
						break;
					}

					out.println("        Options:");
					out.println("            (c)reate index in the background");
					out.println("            (c!)reate index in the foreground");
					out.println("            (s)kip this index");
					out.println("            (r)eport on other indexes without further prompts");
					out.println("            (q)uit this program");
					out.print("        Your choice: ");

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
						out.println();
						err.println("    *** ERROR: unknown choice \"" + input
								+ "\".");
						out.println();
					}
				}
			} catch (IOException e) {
				err.println("Error: " + e.getLocalizedMessage());
				err.println("Switching into report-only mode for remainder of run.");
				reportOnly = true;
			} catch (MongoException e) {
				err.println("Error: " + e.getLocalizedMessage());
				err.println("Switching into report-only mode for remainder of run.");
				reportOnly = true;
			}
		}
	}

	class AutoIndexActions implements IndexActions {
		final long maxCount;

		List<UnavailableIndex> unavailableIndexes = new LinkedList<UnavailableIndex>();

		class UnavailableIndex {
			String serviceName;
			String indexName;

			UnavailableIndex(String serviceName, String indexName) {
				this.serviceName = serviceName;
				this.indexName = indexName;
			}
		}

		public AutoIndexActions(long maxCount) {
			this.maxCount = maxCount;
		}

		@Override
		public void notMongoIndexable(String name) {
			LOGGER.debug(name + " is not MongoIndexable");
		}

		@Override
		public void checkingIntro(String name) {
			// empty
		}

		@Override
		public void hasIndex(IndexDescription pref) {
			LOGGER.debug(pref.getName() + " already exists");
		}

		@Override
		public void doesNotHaveIndex(MongoIndexable service,
				String serviceName, IndexDescription pref) {
			final long count = service.getDocumentCount();
			final String combinedName = serviceName + ":" + pref.getName();
			LOGGER.debug(combinedName + " does not exist; collection has "
					+ count + " documents");
			if (count <= maxCount) {
				LOGGER.info("creating " + combinedName + " in background");
				ensureIndex(service, pref, true);
			} else {
				unavailableIndexes.add(new UnavailableIndex(serviceName, pref
						.getName()));
				LOGGER.debug("will not create " + combinedName);
			}
		}
	}

	boolean indexesMatch(IndexDescription pref, DBObject actual) {
		DBObject actualKey = (DBObject) actual.get("key");
		return pref.getDescription().equals(actualKey);
	}

	void ensureIndex(MongoIndexable service, IndexDescription desiredIndex,
			boolean inBackground) throws MongoException {
		final DBObject options = BasicDBObjectBuilder.start()
				.add("name", desiredIndex.getName())
				.add("unique", desiredIndex.isUnique())
				.add("background", inBackground).get();
		service.ensureIndex(desiredIndex.getDescription(), options);
	}

	void ensureIndexes(MongoIndexable service,
			List<IndexDescription> desiredIndexes, boolean inBackground)
			throws MongoException {
		for (IndexDescription index : desiredIndexes) {
			ensureIndex(service, index, inBackground);
		}
	}

	void matchIndexes(IndexActions actions, MongoIndexable service,
			String serviceName, List<IndexDescription> preferredIndexes,
			List<DBObject> actualIndexes) throws QuitException {
		preferred: for (IndexDescription pref : preferredIndexes) {
			for (DBObject actual : actualIndexes) {
				if (indexesMatch(pref, actual)) {
					actions.hasIndex(pref);
					continue preferred;
				}
			}
			actions.doesNotHaveIndex(service, serviceName, pref);
		}
	}

	void doCheckService(IndexActions actions, MongoIndexable indexable,
			String serviceName) throws QuitException {
		List<IndexDescription> preferredIndexes = indexable
				.getPreferredIndexes();
		List<DBObject> actualIndexes = indexable.getIndexInfo();

		matchIndexes(actions, indexable, serviceName, preferredIndexes,
				actualIndexes);
	}

	void checkService(IndexActions actions, String name, Object service)
			throws QuitException {
		if (!(service instanceof MongoIndexable)) {
			actions.notMongoIndexable(name);
			return;
		}

		actions.checkingIntro(name);
		MongoIndexable indexable = (MongoIndexable) service;
		doCheckService(actions, indexable, name);
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

	public void runCheckAndAutoEnsure(long maxDocuments) {
		AutoIndexActions actions = new AutoIndexActions(maxDocuments);
		try {
			for (Service service : SERVICES) {
				final Object theService = context.getBean(service.name,
						service.klass);
				checkService(actions, service.name, theService);
			}
		} catch (QuitException e) {
			// empty
		}

		if (!actions.unavailableIndexes.isEmpty()) {
			boolean first = true;
			StringBuffer list = new StringBuffer();

			for (AutoIndexActions.UnavailableIndex ui : actions.unavailableIndexes) {
				if (!first) {
					list.append(", ");
				} else {
					first = false;
				}
				list.append(ui.serviceName + ":" + ui.indexName);
			}

			LOGGER.warn("Please run Enkive's MongoDB index tool; Enkive's MongoDB is missing "
					+ actions.unavailableIndexes.size()
					+ " index(es) ["
					+ list
					+ "]");
		}
	}

	public void runConsole() {
		try {
			ConsoleIndexActions actions = new ConsoleIndexActions(System.in,
					System.out, System.err);

			for (Service service : SERVICES) {
				final Object theService = context.getBean(service.name,
						service.klass);
				checkService(actions, service.name, theService);
			}

			System.out.println("Done; all indexes checked.");
		} catch (QuitException e) {
			System.out.println("Quitting at user request.");
		}
	}

	public static void checkAndAutoEnsureMongoIndexes(
			AbstractApplicationContext context, long maxDocuments) {
		MongoDBIndexManager manager = new MongoDBIndexManager(context);
		manager.runCheckAndAutoEnsure(maxDocuments);
	}

	public static void checkAndAutoEnsureMongoIndexes(
			AbstractApplicationContext context) {
		checkAndAutoEnsureMongoIndexes(context, MAX_DOCS_FOR_AUTO_ENSURE_INDEX);
	}

	public static void main(String[] args) {
		final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				CONFIG_FILES);
		context.registerShutdownHook();

		new MongoDBIndexManager(context).runConsole();

		context.close();
	}
}
