package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.QUEUE_LENGTH;
import static com.linuxbox.enkive.statistics.StatsConstants.STATISTIC_CHECK_ERROR;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.linuxbox.enkive.statistics.VarsMaker;

public class UnixPostfixQueueStatisticsService extends AbstractGatherer {

	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";

	public UnixPostfixQueueStatisticsService(String serviceName, String schedule) {
		super(serviceName, schedule);
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println(getStatistics());
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

	@Override
	public Map<String, Object> getStatistics() {
		Map<String, Object> result = VarsMaker.createMap();
		try {
			result.put(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			result.put(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
		}
		return result;
	}

	@Override
	public Map<String, Object> getStatistics(String[] keys) {
		if (keys == null) {
			return getStatistics();
		}
		Map<String, Object> stats = getStatistics();
		Map<String, Object> selectedStats = VarsMaker.createMap();
		for (String key : keys) {
			if (stats.get(key) != null) {
				selectedStats.put(key, stats.get(key));
			}
		}
		selectedStats
				.put(STAT_TIME_STAMP, new Date(System.currentTimeMillis()));

		return selectedStats;
	}
}
