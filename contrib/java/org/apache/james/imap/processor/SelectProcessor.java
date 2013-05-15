/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.imap.processor;

import org.apache.james.imap.api.ImapCommand;
import org.apache.james.imap.api.display.HumanReadableText;
import org.apache.james.imap.api.message.response.StatusResponseFactory;
import org.apache.james.imap.api.process.ImapProcessor;
import org.apache.james.imap.api.process.ImapSession;
import org.apache.james.imap.api.process.ImapProcessor.Responder;
import org.apache.james.imap.message.request.ExamineRequest;
import org.apache.james.imap.message.request.SelectRequest;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.exception.MailboxNotFoundException;
import org.apache.james.mailbox.model.MailboxPath;

public class SelectProcessor extends AbstractSelectionProcessor<SelectRequest> {

    public SelectProcessor(final ImapProcessor next, final MailboxManager mailboxManager, final StatusResponseFactory statusResponseFactory) {
        super(SelectRequest.class, next, mailboxManager, statusResponseFactory, false);
    }

    protected void doProcess(SelectRequest request, ImapSession session, String tag, ImapCommand command, Responder responder) {
        final String mailboxName = request.getMailboxName();
        try {
            final MailboxPath fullMailboxPath = buildFullPath(session, mailboxName);

            respond(tag, command, session, fullMailboxPath, request, responder);           
            
        } catch (MailboxNotFoundException e) {
            session.getLog().debug("Select failed as mailbox does not exist " + mailboxName, e);
            responder.respond(statusResponseFactory.taggedNo(tag, command, HumanReadableText.FAILURE_NO_SUCH_MAILBOX));
        } catch (MailboxException e) {
            session.getLog().info("Select failed for mailbox " + mailboxName , e);
            no(command, tag, responder, HumanReadableText.SELECT);
        } 
    }
}
