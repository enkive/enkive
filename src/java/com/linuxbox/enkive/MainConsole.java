package com.linuxbox.enkive;

import static com.linuxbox.enkive.Copyright.COPYRIGHT;
import static com.linuxbox.enkive.Copyright.LICENSE;
import static com.linuxbox.enkive.Copyright.PRODUCT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class MainConsole extends Main {
	static final String CONSOLE_PROMPT = "enkive> ";
	static final String[] CONFIG_FILES = { "enkive-server.xml" };

	private BufferedReader in;
	private PrintStream out;

	private Set<String> stopCommandSet = new HashSet<String>();
	private String shutdownReason;

	public MainConsole(String[] arguments) {
		super(CONFIG_FILES, arguments);

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
	protected void startup() {
		out.println(PRODUCT);
		out.println(COPYRIGHT);
		out.println(LICENSE);
	}

	@Override
	protected void shutdown() {
		System.out.println("Enkive shutting down (" + shutdownReason + ")...");
	}

	@Override
	protected void doEventLoop() {
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

	public static void main(String[] arguments) {
		Main main = new MainConsole(arguments);
		main.run();
	}
}
