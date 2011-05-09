package com.linuxbox.enkive;

import static com.linuxbox.enkive.Copyright.COPYRIGHT;
import static com.linuxbox.enkive.Copyright.LICENSE;
import static com.linuxbox.enkive.Copyright.PRODUCT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	static final String CONSOLE_PROMPT = "enkive> ";
	static final String[] CONFIG_FILES = { "enkive-properties.xml",
			"enkive-server.xml" };

	public static void main(String[] arguments) {
		System.out.println(PRODUCT);
		System.out.println(COPYRIGHT);
		System.out.println(LICENSE);

		AbstractApplicationContext context = new ClassPathXmlApplicationContext(
				CONFIG_FILES);
		context.registerShutdownHook();

		String shutdownReason = "UNKNOWN";
		Set<String> stopCommandSet = new HashSet<String>();
		stopCommandSet.add("shutdown");
		stopCommandSet.add("stop");
		stopCommandSet.add("exit");
		stopCommandSet.add("quit");
		stopCommandSet.add("end");

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
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
			shutdownReason = "received I/O exception on console";
		}

		System.out.println("Enkive shutting down (" + shutdownReason + ")...");
	}
}
