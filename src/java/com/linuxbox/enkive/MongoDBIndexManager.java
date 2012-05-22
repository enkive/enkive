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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.linuxbox.util.mongodb.MongoIndexable;
import com.linuxbox.util.mongodb.MongoIndexable.IndexDescription;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * This class tries to determine whether all preferred MongoDB indexes for a
 * given service exist. And if a given index does not exist, it can be created.
 * The core code has to be shared by two different functions. One is a
 * command-line tool to manage indexes. The other is during start-up. The
 * developers of MongoDB have indicated it's bad practice simply to call
 * ensure_index during start-up, as creating an index could take quite a while,
 * and if it's created in the foreground, the DB is otherwise unusable while the
 * index is being created. So we provide a menu-based system administration tool
 * that will analyze the indexes and offer to create (ensure) any that are
 * missing. Also, to simply system administration, when Enkive starts up, it can
 * check for missing indexes and create those if there are sufficiently few
 * documents (so it won't take very long).
 */
public class MongoDBIndexManager implements ApplicationContextAware {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive");

	/**
	 * The xml file to use if we need to build our own beans and context.
	 */
	static final String[] CONFIG_FILES = { "enkive-server.xml" };

	/**
	 * How many documents are allowed in a collection if we automatically ensure
	 * that the index exists.
	 */
	static public final long MAX_DOCS_FOR_AUTO_ENSURE_INDEX_DEFAULT = 100;

	/**
	 * The application context we're using.
	 */
	ApplicationContext applicationContext;

	List<Object> initialPotentialServices;
	Map<String, Object> potentialServices;
	long maxDocumentsForAutoEnsure = MAX_DOCS_FOR_AUTO_ENSURE_INDEX_DEFAULT;

	/**
	 * Default constructor.
	 * 
	 * @param context
	 */
	public MongoDBIndexManager() {
		potentialServices = new HashMap<String, Object>();
	}

	/**
	 * If the creator wants to control the services checked, this constructor
	 * should be used. Otherwise all services that implement MongoIndexable will
	 * be checked.
	 */
	public MongoDBIndexManager(List<Object> services) {
		this();
		initialPotentialServices = new LinkedList<Object>();
		initialPotentialServices.addAll(services);
	}

	/**
	 * Thrown when the user has decided to quit.
	 */
	static class QuitException extends Exception {
		private static final long serialVersionUID = 6931399708421895460L;
		// empty
	}

	/**
	 * We want the index checking code to be run both as a console-based tool
	 * and as part of the start-up of the system. So this interface describes
	 * the methods that will define the different actions in these two
	 * circumstances.
	 */
	interface IndexActions {
		/**
		 * Called if a service is not MongoIndexable.
		 */
		public void notMongoIndexable(String name);

		/**
		 * Called if a service is MongoIndexable.
		 */
		public void isMongoIndexable(String name);

		/**
		 * Called if a service has a preferred index.
		 */
		public void hasIndex(IndexDescription pref);

		/**
		 * Called if a service does not have a preferred index.
		 */
		public void doesNotHaveIndex(MongoIndexable service,
				String serviceName, IndexDescription pref) throws QuitException;
	}

	/**
	 * These are the actions that the console tool will use.
	 */
	class ConsoleIndexActions implements IndexActions {
		BufferedReader in;
		PrintStream out;
		PrintStream err;

		/**
		 * Whether in report-only mode -- no further queries made to the user.
		 */
		boolean reportOnly;

		public ConsoleIndexActions(InputStream input, PrintStream output,
				PrintStream error) {
			this.in = new BufferedReader(new InputStreamReader(input));
			this.out = output;
			this.err = error;
			reportOnly = false;
		}

		@Override
		public void notMongoIndexable(String name) {
			out.println(name + " is not a MongoDB indexable service.");
		}

		@Override
		public void isMongoIndexable(String name) {
			out.println("Checking indexes for " + name + "....");
		}

		@Override
		public void hasIndex(IndexDescription pref) {
			out.println("    Index \"" + pref.getName() + "\" exists.");

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
		public void isMongoIndexable(String name) {
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

	/**
	 * Returns true if the preferred index matches the index found.
	 */
	boolean indexesMatch(IndexDescription pref, DBObject actual) {
		DBObject actualKey = (DBObject) actual.get("key");
		return pref.getDescription().equals(actualKey);
	}

	/**
	 * Run the MongoDB ensure_index function on the service provided, creating
	 * the index provided. Can be ensured in the foreground or background
	 * depending on value of background.
	 */
	void ensureIndex(MongoIndexable service, IndexDescription desiredIndex,
			boolean inBackground) throws MongoException {
		final DBObject options = BasicDBObjectBuilder.start()
				.add("name", desiredIndex.getName())
				.add("unique", desiredIndex.isUnique())
				.add("background", inBackground).get();
		service.ensureIndex(desiredIndex.getDescription(), options);
	}

	/**
	 * Given a service, find out both its preferred indexes and the indexes it
	 * actually has. Handle any of the missing preferred indexes accordingly.
	 */
	void doCheckService(IndexActions actions, MongoIndexable service,
			String serviceName) throws QuitException {
		List<IndexDescription> preferredIndexes = service.getPreferredIndexes();
		List<DBObject> actualIndexes = service.getIndexInfo();

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

	/**
	 * See if a service is or is not MongoIndexable. If it is, check its
	 * indexes.
	 */
	void checkService(IndexActions actions, String name, Object service)
			throws QuitException {
		if (!(service instanceof MongoIndexable)) {
			actions.notMongoIndexable(name);
			return;
		}

		actions.isMongoIndexable(name);
		MongoIndexable indexable = (MongoIndexable) service;

		doCheckService(actions, indexable, name);
	}

	public long getMaxDocumentsForAutoEnsure() {
		return maxDocumentsForAutoEnsure;
	}

	public void setMaxDocumentsForAutoEnsure(long maxDocumentsForAutoEnsure) {
		this.maxDocumentsForAutoEnsure = maxDocumentsForAutoEnsure;
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

	public void loadServices() {
		if (initialPotentialServices != null) {
			for (Object o : initialPotentialServices) {
				final String[] names = applicationContext.getBeanNamesForType(o
						.getClass());
				final String name = names.length == 1 ? names[0] : o.getClass()
						.getSimpleName();

				potentialServices.put(name, o);
			}
			initialPotentialServices.clear();
			initialPotentialServices = null;
		} else {
			Map<String, MongoIndexable> mongoIndexableBeans = applicationContext
					.getBeansOfType(MongoIndexable.class);
			for (Entry<String, MongoIndexable> p : mongoIndexableBeans
					.entrySet()) {
				potentialServices.put(p.getKey(), p.getValue());
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		this.applicationContext = context;
	}

	public void runCheckAndAutoEnsure() {
		runCheckAndAutoEnsure(MAX_DOCS_FOR_AUTO_ENSURE_INDEX_DEFAULT);
	}

	public void runCheckAndAutoEnsure(long maxDocuments) {
		AutoIndexActions actions = new AutoIndexActions(maxDocuments);
		try {
			loadServices();
			for (Entry<String, Object> service : potentialServices.entrySet()) {
				checkService(actions, service.getKey(), service.getValue());
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

	void runConsole() {
		try {
			loadServices();
			ConsoleIndexActions actions = new ConsoleIndexActions(System.in,
					System.out, System.err);

			for (Entry<String, Object> service : potentialServices.entrySet()) {
				checkService(actions, service.getKey(), service.getValue());
			}

			System.out.println("Done; all indexes checked.");
		} catch (QuitException e) {
			System.out.println("Quitting at user request.");
		}
	}

	public static void main(String[] args) {
		final AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				CONFIG_FILES);
		context.registerShutdownHook();

		final MongoDBIndexManager indexManager = context
				.getBean(MongoDBIndexManager.class);
		indexManager.runConsole();

		context.close();
	}
}
