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
package org.apache.james.mailbox.store.quota;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.Quota;
import org.apache.james.mailbox.store.StoreMailboxManager;

/**
 * {@link ListeningQuotaManager} which use the same quota for all users.
 * 
 * By default this means not quota at all
 */
public class FixedQuotaManager extends ListeningQuotaManager{

    @SuppressWarnings("rawtypes")
    public FixedQuotaManager(StoreMailboxManager manager) throws MailboxException {
        super(manager);
    }

    private long maxStorage = Quota.UNLIMITED;
    private long maxMessage = Quota.UNLIMITED;

    public void setMaxStorage(long maxStorage) {
        this.maxStorage = maxStorage;
    }
    
    public void setMaxMessage(long maxMessage) {
        this.maxMessage = maxMessage;
    }
    
    @Override
    protected long getMaxStorage(MailboxSession session) throws MailboxException {
        return maxStorage;
    }

    @Override
    protected long getMaxMessage(MailboxSession session) throws MailboxException {
        return maxMessage;
    }

}
