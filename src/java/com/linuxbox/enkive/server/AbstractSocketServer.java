/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.mailprocessor.ThreadedProcessor;

public abstract class AbstractSocketServer implements EnkiveServer {
	// protected static final int LISTEN_BACKLOG = 10;
	protected int LISTEN_BACKLOG;
	protected static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.server");

	protected final int port;
	protected final String serviceName;
	protected ServerSocket serverSocket;
	private Thread serverThread;

	public AbstractSocketServer(String serviceName, int port, int listenBacklog) {
		super();
		this.serviceName = serviceName;
		this.port = port;
		this.LISTEN_BACKLOG = listenBacklog;
	}

	public void startServer() {
		serverThread = new Thread(this);
		serverThread.setName(serviceName + " server");
		serverThread.start();
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " server started");
	}

	public void shutdownServer() {
		// closing the server socket will wake up the waiting server thread
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException ex) {
			// empty
		} finally {
			serverSocket = null;
		}
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " server shutdown initiated");

		if (serverThread != null) {
			try {
				serverThread.join();
			} catch (Exception e) {
				// empty
			}
		}
		if (LOGGER.isTraceEnabled())
			LOGGER.trace(serviceName + " server shutdown completed");
	}

	public boolean hasShutdown() {
		return serverSocket.isClosed();
	}

	public void run() {
		Socket sessionSocket = null;
		boolean hasSocket = false;
		try {
			serverSocket = new ServerSocket(port, LISTEN_BACKLOG);
			serverSocket.setReceiveBufferSize(8);
			hasSocket = true;

			while (!hasShutdown()) {
				// TODO Eric : change this so there's a timeout and we can shut
				// down more cleanly than closing sockets out from under a
				// thread
				sessionSocket = serverSocket.accept(); // wait for connection

				// got connection!

				sessionSocket.setTcpNoDelay(true);

				createAndStartProcessor(sessionSocket);
				if (LOGGER.isTraceEnabled())
					LOGGER.trace(serviceName + " processor/session started");
			}
		} catch (SocketException e) {
			// will get a SocketException when the server socket is closed
			// LOGGER.debug("SocketException", e); catch it separately so it
			// isn't caught below in the catch for general exceptions
			if (!hasSocket) {
				LOGGER.error("unexpected socket exception", e);
			}
		} catch (Exception e) {
			LOGGER.error("Error running server or launching session.", e);
		} catch (Throwable e) {
			LOGGER.fatal("Unexpected error in server thread.", e);
		} finally {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(serviceName + " server has left run loop");
			}
		}

		shutdownProcessors();
	}

	/**
	 * A call-back to notify the server that the other side closed down the
	 * connection. TODO: should this be pushed down to AbstractSocketServer???
	 * 
	 * @param processor
	 */
	public abstract void processorClosed(ThreadedProcessor processor);

	/**
	 * Responsible for shutting down any processors that are still running.
	 */
	protected abstract void shutdownProcessors();

	/**
	 * Responsible for creating and setting up the management of the processor
	 * that feeds off the given socket.
	 * 
	 * @param socket
	 * @throws Exception
	 */
	protected abstract void createAndStartProcessor(Socket socket)
			throws Exception;
}
