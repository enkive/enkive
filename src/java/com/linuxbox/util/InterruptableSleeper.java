/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
package com.linuxbox.util;

/**
 * This class represents something that the user can use to delay for a
 * specified amount of time until it is interrupted. So imagine a thread that
 * does some work and then sleeps for a while before polling for more work. If
 * you're shutting down this thread, you want it to finish what it's currently
 * working on (so you don't want to interrupt the thread), but you want it to
 * stop waiting ASAP. You would send an interrupt to this Object and you can
 * break out of the loop by testing wasInterrupted.
 * 
 * @author ivancich
 * 
 */
public class InterruptableSleeper extends Thread {
	// five minute interval
	private static final long DEFAULT_LOOP_INTERVAL = 5 * 60 * 1000;
	private static final String THREAD_NAME = "InterruptableSleeper";

	private boolean wasInterrupted;
	private long loopInterval;
	private long postInterruptionWaits;

	public InterruptableSleeper() {
		this(DEFAULT_LOOP_INTERVAL);
	}

	public InterruptableSleeper(long loopInterval) {
		super(THREAD_NAME);
		this.loopInterval = loopInterval;
		wasInterrupted = false;
		postInterruptionWaits = 0;
	}

	// this thread just sleeps; it's primary purpose in life is to be
	// interrupted
	public void run() {
		try {
			while (true) {
				sleep(loopInterval);
			}
		} catch (InterruptedException e) {
			wasInterrupted = true;
		}
	}

	public void waitFor(long milliseconds) {
		if (wasInterrupted) {
			postInterruptionWaits++;
		}

		if (postInterruptionWaits > 1) {
			System.err.println("have called waitFor " + postInterruptionWaits
					+ " times since interruption");

			// delay a little so as not to cause run-away loop
			try {
				final long time = Math.min(milliseconds,
						postInterruptionWaits * 200);
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// do nothing
			}
			return;
		}

		try {
			this.join(milliseconds);
		} catch (InterruptedException e) {
			throw new RuntimeException(
					"reached illegal point in in InterruptableSleeper");
		}
	}

	public boolean wasInterrupted() {
		return wasInterrupted;
	}
}
