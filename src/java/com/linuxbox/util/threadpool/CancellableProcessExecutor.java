package com.linuxbox.util.threadpool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CancellableProcessExecutor {

	/*** VARIABLES ***/

	private ThreadPoolExecutor executor;

	/**
	 * Maps identifiers to CPFutures. Should only be manipulated by helper
	 * methods.
	 */
	private Map<String, CPFuture<?>> identifierMap;

	/**
	 * Maps Runnables (likely Futures returned by submitting to the executor) to
	 * identifiers. Should only be manipulated by helper methods.
	 */
	private Map<Runnable, String> runnableMap;

	/*** INNER CLASSES ***/

	/**
	 * A CPFuture encapsulates both a Future and an AbstractCancellableProcess.
	 * The cancel-able process might need to run clean-up code after it is
	 * canceled, and so therefore needs to be notified in an appropriate way,
	 * even if the Runnable/Callable encapsulated by a Future is still in the
	 * queue while it's canceled. So the cancel method must do some extra work.
	 * 
	 * @author eric
	 * 
	 * @param <U>
	 */
	public class CPFuture<U> implements Future<U> {
		private Future<U> underlyingFuture;
		private AbstractCancellableProcess<U> cancellableProcess;

		public CPFuture(Future<U> delegate,
				AbstractCancellableProcess<U> process) {
			this.underlyingFuture = delegate;
			this.cancellableProcess = process;
		}

		@Override
		public boolean cancel(boolean cancelWhileRunning) {
			if (underlyingFuture instanceof Runnable) {
				final boolean removedFromQueue = executor
						.remove((Runnable) underlyingFuture);

				// if removed from queue, notify process and underlying future
				if (removedFromQueue) {
					cancellableProcess.cancelFromQueue();
					underlyingFuture.cancel(false);
					removeFromMaps(underlyingFuture);
					return true;
				}
			}

			// at this point we know that we did not remove the process from the
			// thread queue

			cancellableProcess.cancelOnceStarted();

			return underlyingFuture.cancel(cancelWhileRunning);
		}

		@Override
		public U get() throws InterruptedException, ExecutionException {
			return underlyingFuture.get();
		}

		@Override
		public U get(long arg0, TimeUnit arg1) throws InterruptedException,
				ExecutionException, TimeoutException {
			return underlyingFuture.get(arg0, arg1);
		}

		@Override
		public boolean isCancelled() {
			return underlyingFuture.isCancelled();
		}

		@Override
		public boolean isDone() {
			return underlyingFuture.isDone();
		}

		public Future<U> getUnderlyingFuture() {
			return underlyingFuture;
		}

		public String getId() {
			return cancellableProcess.getIdentifier();
		}
	}

	/**
	 * This simply exists to clean up our maps after a thread is done by
	 * providing an afterExecute method.
	 * 
	 * @author eric
	 * 
	 */
	class CPThreadPoolExecutor extends ThreadPoolExecutor {
		public CPThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue,
				RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue, handler);
		}

		public CPThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
				RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue, threadFactory, handler);
		}

		public CPThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
					workQueue, threadFactory);
		}

		public CPThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
				long keepAliveTime, TimeUnit unit,
				BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		protected void afterExecute(Runnable r, Throwable t) {
			CPFuture<?> cpFuture = lookup(r);
			if (cpFuture != null) {
				AbstractCancellableProcess<?> process = cpFuture.cancellableProcess;
				process.finishOnceStarted();
			}

			removeFromMaps(r);
		}
	}

	/*** CONSTRUCTORS ***/

	public CancellableProcessExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		this.executor = new CPThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workQueue, handler);
		finishInitialization();
	}

	public CancellableProcessExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
			RejectedExecutionHandler handler) {
		this.executor = new CPThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workQueue, threadFactory, handler);
		finishInitialization();
	}

	public CancellableProcessExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		this.executor = new CPThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workQueue, threadFactory);
		finishInitialization();
	}

	public CancellableProcessExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this.executor = new CPThreadPoolExecutor(corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workQueue);
		finishInitialization();
	}

	/*** API METHODS ***/

	public <T> CPFuture<T> submit(AbstractCancellableProcess<T> process) {
		Future<T> originalFuture = executor.submit(process);
		CPFuture<T> returnedFuture = new CPFuture<T>(originalFuture, process);
		addToMaps(process.getIdentifier(), originalFuture, returnedFuture);
		return returnedFuture;
	}

	/**
	 * Used for debugging purposes to make sure that the maps get cleaned up
	 * after a process is complete.
	 * 
	 * @return
	 */
	public int[] mapStatus() {
		int[] result = new int[2];
		result[0] = identifierMap.size();
		result[1] = runnableMap.size();
		return result;
	}

	public boolean cancel(String processIdentifier, boolean stopWhileRunning) {
		CPFuture<?> toCancel = lookup(processIdentifier);

		if (toCancel != null) {
			// AbstractCancellableProcess<?> process =
			// toCancel.cancellableProcess;
			// process.cancelOnceStarted();
			return toCancel.cancel(stopWhileRunning);
		} else {
			return false;
		}
	}

	/*** DELEGATE METHODS ***/

	public void shutdown() {
		executor.shutdown();
	}

	public int getPoolSize() {
		return executor.getPoolSize();
	}

	public void setCorePoolSize(int size) {
		executor.setCorePoolSize(size);
	}

	public void setMaximumPoolSize(int size) {
		executor.setMaximumPoolSize(size);
	}

	/*** HELPER METHODS ***/

	private void finishInitialization() {
		this.identifierMap = new HashMap<String, CPFuture<?>>();
		this.runnableMap = new HashMap<Runnable, String>();
	}

	private CPFuture<?> lookup(String identifier) {
		return identifierMap.get(identifier);
	}

	private CPFuture<?> lookup(Runnable r) {
		String identifier = runnableMap.get(r);
		if (identifier != null) {
			return lookup(identifier);
		} else {
			return null;
		}
	}

	private void addToMaps(String identifier, Future<?> future,
			CPFuture<?> cpFuture) {
		synchronized (identifierMap) {
			identifierMap.put(identifier, cpFuture);
			if (future instanceof Runnable) {
				runnableMap.put((Runnable) future, identifier);
			}
		}
	}

	private void removeFromMaps(Future<?> future) {
		if (future instanceof Runnable) {
			removeFromMaps((Runnable) future);
		}
	}

	private void removeFromMaps(Runnable r) {
		synchronized (identifierMap) {
			final String identifier = runnableMap.get(r);
			runnableMap.remove(r);
			if (identifier != null) {
				identifierMap.remove(identifier);
			}
		}
	}
}
