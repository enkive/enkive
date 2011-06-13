package com.linuxbox.util;

import java.util.HashMap;
import java.util.Map;

public class ShardingHelper {
	public static class Range {
		private final int low;
		private final int high;

		private Range(int low, int high) {
			this.low = low;
			this.high = high;
		}

		/**
		 * The lowest value in the range. All elements in the range should be
		 * GREATER THAN OR EQUAL TO this value.
		 * 
		 * @return
		 */
		public int getLow() {
			return low;
		}

		/**
		 * The highest value in the range PLUS ONE. All elements in the range
		 * should be LESS THAN (and never equal to) this value.
		 * 
		 * @return
		 */
		public int getHigh() {
			return high;
		}
	}

	private final int valueMax;
	private final int shardCount;
	private final float perServer;
	private Map<Integer, Range> rangeMemo;

	public ShardingHelper(int valueMax, int shardCount) {
		this.valueMax = valueMax;
		this.shardCount = shardCount;
		this.perServer = this.valueMax / (float) this.shardCount;
		this.rangeMemo = new HashMap<Integer, Range>();
	}

	public ShardingHelper(byte valueBits, int shardCount) {
		this(1 << valueBits, shardCount);
	}

	/**
	 * Returns a Range value containing the range of values for the shard key.
	 * The range contains the lower value and 1 + the upper value.
	 * 
	 * @param shardNumber
	 *            a value from 0..(shardCount - 1) to get usable ranges of
	 *            value, although if we wanted to server not to get a shard, we
	 *            could pass in an illegal value (e.g., -1).
	 * @return
	 */
	public Range getRange(int shardNumber) {
		Range result = rangeMemo.get(shardNumber);
		if (result != null) {
			return result;
		}

		final int startRange = Math.round(shardNumber * perServer);
		final int endRange = Math.round((shardNumber + 1) * perServer);
		result = new Range(startRange, endRange);
		rangeMemo.put(shardNumber, result);

		return result;
	}

	public int getShardCount() {
		return shardCount;
	}
}
