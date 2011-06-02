/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.mailprocessor;

import java.net.Socket;

import com.linuxbox.enkive.archiver.MessageArchivingService;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.server.AbstractSocketServer;

public interface ArchivingProcessor extends ThreadedProcessor {
	/**
	 * Initializes an email processor, which will run in its own thread. Will be
	 * called once before start is called to kick off the thread.
	 * 
	 * @param server
	 *            The server that will be notified if the other side of the
	 *            connection closes the socket first.
	 * @param socket
	 *            The socket (not ServerSocket) on which a connection has been
	 *            established. The socket should be ready to create input and
	 *            output streams on which to speak to an (S|L)MTP server.
	 */
	public void initializeProcessor(AbstractSocketServer server, Socket  socket);
}
