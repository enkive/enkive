/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
 *******************************************************************************/
package com.linuxbox.enkive;

import static com.linuxbox.enkive.ProductInfo.COPYRIGHT;
import static com.linuxbox.enkive.ProductInfo.LICENSE;
import static com.linuxbox.enkive.ProductInfo.PRODUCT;
import static com.linuxbox.enkive.ProductInfo.VERSION;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.ApplicationContext;

public class MainConsole extends Main {
	static final String CONSOLE_PROMPT = "enkive> ";
	static final String[] CONFIG_FILES = { "enkive-server.xml" };
	static final String DESCRIPTION = "com.linuxbox.enkive.MainConsole";

	protected BufferedReader in;
	protected PrintStream out;

	private Set<String> stopCommandSet = new HashSet<String>();
	private String shutdownReason;

	public MainConsole(String[] arguments) {
		this(arguments, CONFIG_FILES, DESCRIPTION);
	}

	public MainConsole(String[] arguments, String[] configFiles,
			String description) {
		super(arguments, configFiles, description);

		out = System.out;
		in = new BufferedReader(new InputStreamReader(System.in));

		shutdownReason = "UNKNOWN";

		stopCommandSet.add("shutdown");
		stopCommandSet.add("stop");
		stopCommandSet.add("exit");
		stopCommandSet.add("quit");
		stopCommandSet.add("end");
	}

	@Override
	protected void preStartup() {
		out.println(PRODUCT + " v. " + VERSION);
		out.println(COPYRIGHT);
		out.println(LICENSE);
	}

	@Override
	protected void postStartup() {
		// empty
	}

	@Override
	protected void preShutdown() {
		System.out.println("Enkive shutting down (because " + shutdownReason
				+ ")...");
	}

	@Override
	protected void postShutdown() {
		System.out.println("Enkive has shut down.");
	}

	@Override
	protected void runCoreFunctionality(ApplicationContext context) {
		try {
			String input;

			System.out.print(CONSOLE_PROMPT);
			while ((input = in.readLine().trim()) != null) {
				if (stopCommandSet.contains(input.toLowerCase())) {
					shutdownReason = "\"" + input + "\" entered in console";
					break;
				}
				if (!input.isEmpty()) {
					System.out.println("Error: \"" + input
							+ "\" is not understood.");
				}
				System.out.print(CONSOLE_PROMPT);
			}

			if (input == null) {
				shutdownReason = "received console end-of-file";
			}
		} catch (IOException e) {
			shutdownReason = "received I/O exception on console: "
					+ e.getMessage();
			LOGGER.error(shutdownReason, e);
		}
	}

	public static void main(String[] arguments) throws Exception {
		Main main = new MainConsole(arguments);
		main.run();
	}
}
