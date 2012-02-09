/*
 *  Copyright 2010 The Linux Box Corporation.
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

package com.linuxbox.enkive.filter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static com.linuxbox.enkive.filter.EnkiveFilterConstants.*;
import static com.linuxbox.enkive.filter.EnkiveFilterConstants.FilterDefinitionConstants.*;
import com.linuxbox.enkive.message.Message;

public class EnkiveFiltersBean {

	public final static String ENKIVE_FILTERS_FILENAME = "enkive-filters.xml";
	
	private final static Log logger = LogFactory
			.getLog("com.linuxbox.enkive.messagefilters");

	protected int defaultAction = FilterAction.ALLOW;
	protected Set<EnkiveFilter> filterSet;

	public EnkiveFiltersBean() {
		filterSet = new HashSet<EnkiveFilter>();
	}

	protected void startup() {
		logger.trace("Loading Enkive filters");
		try {
			Resource res = new ClassPathResource(ENKIVE_FILTERS_FILENAME);

			FileInputStream filterFile = new FileInputStream(res.getFile());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(filterFile);
			NodeList filters = doc.getElementsByTagName(FILTER);
			doc.getElementsByTagName(DEFAULT_ACTION).item(0).getTextContent();
			if (doc.getElementsByTagName(DEFAULT_ACTION).item(0)
					.getTextContent().toLowerCase().equals(DENY))
				defaultAction = FilterAction.DENY;

			for (int i = 0; i < filters.getLength(); i++) {
				Element filter = (Element) filters.item(i);
				if (filter.getAttribute(ENABLED).equals(FILTER_TRUE)) {
					int filterAction = 0;
					Node action = filter.getElementsByTagName(ACTION).item(0);
					if (action.getTextContent().toLowerCase().equals(ALLOW))
						filterAction = FilterAction.ALLOW;
					if (action.getTextContent().toLowerCase().equals(DENY))
						filterAction = FilterAction.DENY;
					Node header = filter.getElementsByTagName(HEADER).item(0);
					Node value = filter.getElementsByTagName(VALUE).item(0);

					int filterType = 0;
					int filterComparator = 0;

					if (((Element) value).getAttribute(TYPE).toLowerCase()
							.equals(INTEGER))
						filterType = FilterType.INTEGER;
					else if (((Element) value).getAttribute(TYPE)
							.toLowerCase().equals(TEXT))
						filterType = FilterType.STRING;
					else if (((Element) value).getAttribute(TYPE)
							.toLowerCase().equals(ADDRESS))
						filterType = FilterType.ADDRESS;
					else if (((Element) value).getAttribute(TYPE)
							.toLowerCase().equals(FLOAT))
						filterType = FilterType.FLOAT;
					else if (((Element) value).getAttribute(TYPE)
							.toLowerCase().equals(DATE))
						filterType = FilterType.DATE;

					if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(IS_GREATER_THAN))
						filterComparator = FilterComparator.IS_GREATER_THAN;
					else if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(IS_LESS_THAN))
						filterComparator = FilterComparator.IS_LESS_THAN;
					else if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(CONTAINS))
						filterComparator = FilterComparator.CONTAINS;
					else if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(DOES_NOT_CONTAIN))
						filterComparator = FilterComparator.DOES_NOT_CONTAIN;
					else if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(MATCHES))
						filterComparator = FilterComparator.MATCHES;
					else if (((Element) value).getAttribute(COMPARISON)
							.toLowerCase().equals(DOES_NOT_MATCH))
						filterComparator = FilterComparator.DOES_NOT_MATCH;

					filterSet.add(new EnkiveFilter(header.getTextContent(),
							filterAction, filterType, value.getTextContent(),
							filterComparator, defaultAction));
					logger.info("Enkive filtering by header "
							+ header.getTextContent());
				}
			}
			filterFile.close();
		} catch (FileNotFoundException e) {
			logger.fatal("Could not find enkive-filters.xml, Filters not initialized");
		} catch (IOException e) {
			logger.fatal("Could not read file enkive-filters.xml, Filters not initialized");
		} catch (ParserConfigurationException e) {
			logger.fatal("Could not initialize parser for enkive-filters.xml, Filters not initialized");
		} catch (SAXException e) {
			logger.fatal("Could not parse enkive-filters.xml, Filters not initialized");
		}

	}

	protected void shutdown() {

	}

	public boolean filterMessage(Message message) {
		boolean archiveMessage = true;

		if (filterSet.isEmpty()) {
			if (defaultAction == FilterAction.ALLOW)
				archiveMessage = true;
			else if (defaultAction == FilterAction.DENY)
				archiveMessage = false;
		}

		for (EnkiveFilter filter : filterSet) {
			try {
				String value = message.getParsedHeader()
						.getField(filter.getHeader()).getBody().toString()
						.trim();
				archiveMessage = filter.filter(value);
			} catch (Exception e) {
				// do nothing
			}

			if (archiveMessage == false) {
				logger.trace("Message " + message.getMessageId()
						+ " did not pass filter " + filter.getHeader());
				break;
			}
		}
		return archiveMessage;
	}
}
