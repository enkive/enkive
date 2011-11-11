package com.linuxbox.util.threadpool;

import java.util.concurrent.Callable;

/**
 * A process that can be stopped and returns an object of type R if it
 * completes. Subclasses need to implement the runWithResult method. Subclasses
 * can implement the runAfterCancellation method to do any work that should be
 * done after a cancellation.
 * 
 * @author eric
 * 
 * @param <R>
 */
public abstract class AbstractCancellableProcess<R> implements Callable<R> {
	private String identifier;
	private Status status;
	private boolean cancelled;
	private R result;
	private Exception exception;

	@SuppressWarnings("serial")
	public static class CancelledExecutionException extends Exception {
		public CancelledExecutionException() {
			// empty
		}
	}

	public enum Status {
		NOT_STARTED, RUNNING, COMPLETE, CANCELED, ERROR;
	}

	public AbstractCancellableProcess(String identifier) {
		this.identifier = identifier;
		this.cancelled = false;
		this.status = Status.NOT_STARTED;
		this.result = null;
	}

	public String getIdentifier() {
		return identifier;
	}

	protected boolean wasCancelRequested() {
		if (!cancelled) {
			cancelled = Thread.currentThread().isInterrupted();
		}
		return cancelled;
	}

	/**
	 * This method should be called occasionally by a long-running search
	 * process to test whether someone has canceled the search and then to
	 * initiate the cancellation process by throwing the correct exception.
	 * Ultimately the exception should be caught in the call method, and that
	 * will change the status to CANCELED.
	 * 
	 * @throws CancelledExecutionException
	 */
	public void checkForCancel() throws CancelledExecutionException {
		if (wasCancelRequested()) {
			throw new CancelledExecutionException();
		}
	}

	@Override
	public R call() throws Exception {
		synchronized (this) {
			if (status == Status.NOT_STARTED) {
				status = Status.RUNNING;
			} else {
				return null;
			}
		}

		try {
			result = runWithResult();
			status = Status.COMPLETE;
			return result;
		} catch (InterruptedException e) {
			status = Status.CANCELED;
			return null;
		} catch (CancelledExecutionException e) {
			status = Status.CANCELED;
			return null;
		} catch (Exception e) {
			status = Status.ERROR;
			exception = e;
			runAfterError();
			throw e;
		}
	}

	/**
	 * This method should be called if this process is removed from the queue
	 * before it starts.
	 */
	public void cancelFromQueue() {
		status = Status.CANCELED;
		runAfterCancellation();
	}

	/**
	 * This method should be called if this process is canceled once it's no
	 * longer in the queue. The caller does not know if the call method has yet
	 * been invoked. Due to synchronization, this should not be an issue. If
	 * execution in call is already under way, nothing happens. But if call has
	 * not been called or will never be called, it still notes that this process
	 * was cancelled.
	 */
	public void cancelOnceStarted() {
		synchronized (this) {
			if (status == Status.NOT_STARTED) {
				status = Status.CANCELED;
			}
		}
		cancelled = true;
	}

	/**
	 * This method should be called if this process is successfully canceled
	 * once it's started to do any clean-up work. This will typically be done in
	 * a ThreadPoolExecutor's afterExecute method.
	 */
	public void finishOnceStarted() {
		if (status == Status.CANCELED) {
			runAfterCancellation();
		}
	}

	public Status getStatus() {
		return status;
	}

	public boolean isComplete() {
		return status == Status.COMPLETE;
	}

	public boolean isCanceled() {
		return status == Status.CANCELED;
	}

	public boolean hadError() {
		return status == Status.ERROR;
	}

	public boolean hasFinished() {
		return isComplete() || isCanceled() || hadError();
	}

	public Exception getException() {
		return exception;
	}

	/**
	 * Subclasses will implement this method and return a result. The method
	 * will ideally regularly query the stopRequested variable or
	 * getStopRequested method, and if they find that a stop has been requested,
	 * clean up and throw a StopExecutionException.
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws StoppedExcecutionException
	 */
	protected abstract R runWithResult() throws InterruptedException,
			CancelledExecutionException, Exception;

	/**
	 * Override this method and perform what should be done after the process is
	 * canceled. This is a do-nothing default implementation.
	 */
	protected void runAfterCancellation() {
		// default is empty
	}

	/**
	 * Override this method and perform what should be done after the process
	 * runs and generaets an error. This is a do-nothing default implementation.
	 */
	protected void runAfterError() {
		// default is empty
	}
}
