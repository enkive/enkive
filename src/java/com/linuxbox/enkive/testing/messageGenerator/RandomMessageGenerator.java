package com.linuxbox.enkive.testing.messageGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Random;

public class RandomMessageGenerator extends AbstractMessageGenerator {

	public static String messageBodyDataDirectory = "/home/lee/Storage/Projects/Enkive-3/workspace/Enkive/test/data/gutenbergData";

	public RandomMessageGenerator() {
		super();
	}

	@Override
	protected String generateMessageBody() {
		InputStream is = null;
		StringBuffer messageBody = new StringBuffer();
		try {
			File messageBodyDataDir = new File(messageBodyDataDirectory);
			File[] files = messageBodyDataDir.listFiles();

			Random random = new Random();
			int fileToGet = random.nextInt(files.length);
			System.out.println(fileToGet);
			is = new FileInputStream(files[fileToGet]);

			// -------------------------------------------------------------//
			// Step 4: //
			// -------------------------------------------------------------//
			// Convert the InputStream to a buffered DataInputStream. //
			// Buffering the stream makes the reading faster; the //
			// readLine() method of the DataInputStream makes the reading //
			// easier. //
			// -------------------------------------------------------------//

			BufferedReader dis = new BufferedReader(new InputStreamReader(is));

			// ------------------------------------------------------------//
			// Step 5: //
			// ------------------------------------------------------------//
			// Now just read each record of the input stream, and print //
			// it out. Note that it's assumed that this problem is run //
			// from a command-line, not from an application or applet. //
			// ------------------------------------------------------------//
			String s;
			while ((s = dis.readLine()) != null) {
				messageBody.append(s + System.getProperty("line.separator"));
			}

		} catch (MalformedURLException mue) {

			System.out.println("Ouch - a MalformedURLException happened.");
			mue.printStackTrace();
			System.exit(1);

		} catch (IOException ioe) {

			System.out.println("Oops- an IOException happened.");
			ioe.printStackTrace();
			System.exit(1);

		} finally {

			// ---------------------------------//
			// Step 6: Close the InputStream //
			// ---------------------------------//

			try {
				is.close();
			} catch (IOException ioe) {
				// just going to ignore this one
			}

		} // end of 'finally' clause
		return messageBody.toString();
	}

}
