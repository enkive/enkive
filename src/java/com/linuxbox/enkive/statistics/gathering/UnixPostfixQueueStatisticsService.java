package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.QUEUE_LENGTH;
import static com.linuxbox.enkive.statistics.StatsConstants.STATISTIC_CHECK_ERROR;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class UnixPostfixQueueStatisticsService extends AbstractGatherer
		implements GathererInterface {

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";
	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	public UnixPostfixQueueStatisticsService() {
//		setAttributes();
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = createMap();
		try {
			result.put(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			result.put(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
		}
//		attributes.incrementTime();
		return result;
	}

	protected int getQueueLength() throws IOException {
		String[] cmd = { "/bin/sh", "-c",
				POSTFIX_QUEUE_COMMAND + POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE };
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader bri = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		// Output should be just what we need if it was cut correctly
		String output = bri.readLine();
		return Integer.parseInt(output);
	}

	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null)
			return getStatistics();
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = createMap();
		for (String key : keys) {
			if (stats.get(key) != null)
				selectedStats.put(key, stats.get(key));
		}
		selectedStats.put(STAT_TIME_STAMP, System.currentTimeMillis());

//		attributes.incrementTime();
		return selectedStats;
	}
}
