package com.linuxbox.enkive.statistics.message;

import static com.linuxbox.enkive.statistics.StatsConstants.QUEUE_LENGTH;
import static com.linuxbox.enkive.statistics.StatsConstants.STATISTIC_CHECK_ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.linuxbox.enkive.statistics.StatsService;

public class UnixPostfixQueueStatisticsService implements StatsService {

	// public static String POSTFIX_QUEUE_COMMAND = "postqueue -p";
	public static String POSTQUEUE_OUTPUT_MANIPULATOR_PIPELINE = " | grep Requests | cut -d' ' -f 5";
	public static String POSTFIX_QUEUE_COMMAND = "cat /tmp/test.txt";

	@Override
	public JSONObject getStatisticsJSON() throws JSONException {
		JSONObject result = new JSONObject();
		try {
			result.append(QUEUE_LENGTH, getQueueLength());
		} catch (IOException e) {
			result.append(QUEUE_LENGTH + STATISTIC_CHECK_ERROR, e.toString());
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
	
	public JSONObject getStatisticsJSON(Map<String,String> map) throws JSONException{
		//TODO: Implement
		return getStatisticsJSON();
	}
}
