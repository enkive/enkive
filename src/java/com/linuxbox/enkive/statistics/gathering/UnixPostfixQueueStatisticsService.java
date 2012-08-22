package com.linuxbox.enkive.statistics.gathering;

import static com.linuxbox.enkive.statistics.StatsConstants.QUEUE_LENGTH;
import static com.linuxbox.enkive.statistics.StatsConstants.STATISTIC_CHECK_ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.linuxbox.enkive.statistics.PointRawStats;
import com.linuxbox.enkive.statistics.VarsMaker;
import com.linuxbox.enkive.statistics.RawStats;

public class UnixPostfixQueueStatisticsService extends AbstractGatherer {

	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";

	public UnixPostfixQueueStatisticsService(String serviceName,
			String humanName, String schedule, List<String> keys) throws GathererException {
		super(serviceName, humanName, schedule, keys);
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
	public RawStats getStatistics() {
		Map<String, Object> stats = VarsMaker.createMap();
		try {
			stats.put(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			stats.put(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
		}
		RawStats result = new PointRawStats(stats, new Date());
		return result;
	}

	@Override
	public RawStats getStatistics(String[] keys) {
		if (keys == null) {
			return getStatistics();
		}
		Map<String, Object> stats = getStatistics().getStatsMap();
		Map<String, Object> selectedStats = VarsMaker.createMap();

		for (String key : keys) {
			if (stats.get(key) != null) {
				selectedStats.put(key, stats.get(key));
			}
		}
		RawStats result = new PointRawStats(selectedStats, new Date());
		return result;
	}
}
