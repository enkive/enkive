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
package org.apache.james.mailbox;

import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Quota;


/**
 * Allows to get quotas for {@link MailboxSession} which are bound to a user.
 * 
 */
public interface QuotaManager {

    /**
     * Return the message count {@link Quota} for the given {@link MailboxSession} (which in fact is 
     * bound to a user)
     * 
     * @param session
     * @return quota
     * @throws MailboxException
     */
    public Quota getMessageQuota(MailboxSession session) throws MailboxException;

    
    /**
     * Return the message storage {@link Quota} for the given {@link MailboxSession} (which in fact is 
     * bound to a user)
     * 
     * @param session
     * @return quota
     * @throws MailboxException
     */
    public Quota getStorageQuota(MailboxSession session) throws MailboxException;
    
}
