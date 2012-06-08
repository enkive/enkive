package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.QUEUE_LENGTH;
import static com.linuxbox.enkive.statistics.StatsConstants.STATISTIC_CHECK_ERROR;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UnixPostfixQueueStatisticsService extends AbstractGatherer{

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println(getStatistics());
	}

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";
	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	public UnixPostfixQueueStatisticsService(String serviceName, String Schedule) {
		Map<String, String> keys = new HashMap<String, String>();
		keys.put(QUEUE_LENGTH, "AVG");
		keys.put(STAT_TIME_STAMP, "AVG");
		keys.put(STAT_NAME, null);
	}

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = createMap();
		try {
			result.put(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			result.put(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
		}
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

		return selectedStats;
	}
}
