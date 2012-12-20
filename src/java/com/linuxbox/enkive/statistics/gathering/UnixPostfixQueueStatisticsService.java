/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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

import com.linuxbox.enkive.statistics.VarsMaker;

public class UnixPostfixQueueStatisticsService extends AbstractGatherer {

	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";

	public UnixPostfixQueueStatisticsService(String serviceName,
			String humanName, List<String> keys) throws GathererException {
		super(serviceName, humanName, keys);
	}

	public void execute(JobExecutionContext arg0) throws GathererException {
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
	protected Map<String, Object> getPointStatistics(Date startTimestamp,
			Date endTimestamp) throws GathererException {
		Map<String, Object> stats = VarsMaker.createMap();
		try {
			stats.put(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			stats.put(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
		}
		return stats;
	}

	@Override
	protected Map<String, Object> getIntervalStatistics(Date lowerTimestamp,
			Date upperTimestamp) throws GathererException {
		return null;
	}
}
