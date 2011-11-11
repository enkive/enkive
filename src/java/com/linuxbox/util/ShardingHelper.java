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

	/**
	 * Creates a sharding helper with the largest (+1) shard value, and the
	 * number of shards.
	 * 
	 * @param maxShardValue
	 *            this is one more than the highest value of the shard range
	 *            (e.g., if this is 128, then the shard values range from 0 to
	 *            127).
	 * @param shardCount
	 */
	public ShardingHelper(int maxShardValue, int shardCount) {
		this.valueMax = maxShardValue;
		this.shardCount = shardCount;
		this.perServer = this.valueMax / (float) this.shardCount;
		this.rangeMemo = new HashMap<Integer, Range>();
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

	/**
	 * When the shard value is determined by the number of bits, this factory
	 * method can be used in lieu of a constructor. Unfortunately, its two
	 * numeric values are similar to the two numeric values that the constructor
	 * takes, making it easy to invoke the wrong constructor. So it's a factory
	 * method instead.
	 * 
	 * @param valueBits
	 * @param shardCount
	 * @return
	 */
	public static ShardingHelper createWithBitCount(int valueBits,
			int shardCount) {
		return new ShardingHelper(1 << valueBits, shardCount);
	}
}
