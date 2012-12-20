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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.james.mailbox.MailboxListener;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.QuotaManager;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MailboxPath;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.Quota;
import org.apache.james.mailbox.store.MailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.mail.MessageMapper.FetchType;

/**
 * {@link QuotaManager} which will keep track of quota by listing for {@link org.apache.james.mailbox.MailboxListener.Event}'s.
 * 
 * The whole quota is keeped in memory after it was lazy-fetched on the first access
 *  *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class ListeningQuotaManager implements QuotaManager, MailboxListener{

    private MailboxSessionMapperFactory factory;
    private ConcurrentHashMap<String, AtomicLong> counts = new ConcurrentHashMap<String, AtomicLong>();
    private ConcurrentHashMap<String, AtomicLong> sizes = new ConcurrentHashMap<String, AtomicLong>();
    private boolean calculateWhenUnlimited = false;
    
    public ListeningQuotaManager(StoreMailboxManager<?> manager) throws MailboxException {
        this.factory = manager.getMapperFactory();
        manager.addGlobalListener(this, null);
    }
    
    protected MailboxSessionMapperFactory<?> getFactory() {
        return factory;
    }
    
    public void setCalculateUsedWhenUnlimited(boolean calculateWhenUnlimited) {
        this.calculateWhenUnlimited  = calculateWhenUnlimited;
    }

    
    @Override
    public Quota getMessageQuota(MailboxSession session) throws MailboxException {
        long max = getMaxMessage(session);
        if (max != Quota.UNLIMITED || calculateWhenUnlimited) {

            String id = session.getUser().getUserName();
            AtomicLong count = counts.get(id);
            if (count == null) {
                long mc = 0;
                List<Mailbox> mailboxes = factory.getMailboxMapper(session).findMailboxWithPathLike(new MailboxPath(session.getPersonalSpace(), id, "%"));
                for (int i = 0; i < mailboxes.size(); i++) {
                    mc += factory.getMessageMapper(session).countMessagesInMailbox(mailboxes.get(i));
                }
                AtomicLong c = counts.putIfAbsent(id, new AtomicLong(mc));
                if (c != null) {
                    count = c;
                }
            }
            return QuotaImpl.quota(max, count != null ? count.get() : 0);
        } else {
            return QuotaImpl.unlimited();
        }
    }

    @Override
    public Quota getStorageQuota(MailboxSession session) throws MailboxException {
        long max = getMaxStorage(session);
        if (max != Quota.UNLIMITED || calculateWhenUnlimited) {
            MessageMapper mapper = factory.getMessageMapper(session);
        	String id = session.getUser().getUserName();
            AtomicLong size = sizes.get(id);
            
            if (size == null) {
                final AtomicLong mSizes = new AtomicLong(0);
                List<Mailbox> mailboxes = factory.getMailboxMapper(session).findMailboxWithPathLike(new MailboxPath(session.getPersonalSpace(), id, "%"));
                for (int i = 0; i < mailboxes.size(); i++) {
                	long messageSizes = 0;
                    Iterator<Message>  messages = mapper.findInMailbox(mailboxes.get(i), MessageRange.all(), FetchType.Metadata, -1);
                    
                    while(messages.hasNext()) {
                        messageSizes +=  messages.next().getFullContentOctets();
                    }
                    mSizes.set(mSizes.get() + messageSizes);
                }

                AtomicLong s = sizes.putIfAbsent(id, mSizes);
                if (s != null) {
                    size = s;
                } else {
                    size = mSizes;
                }
            }
            return QuotaImpl.quota(max, size.get());
        } else {
            return QuotaImpl.unlimited();
        }
    }
    
    /**
     * Return the maximum storage which is allowed for the given {@link MailboxSession} (in fact the user which the session is bound to)
     * 
     * The returned valued must be in <strong>bytes</strong>
     * 
     * @param session
     * @return maxBytes
     * @throws MailboxException
     */
    protected abstract long getMaxStorage(MailboxSession session) throws MailboxException;
    
    
    /**
     * Return the maximum message count which is allowed for the given {@link MailboxSession} (in fact the user which the session is bound to)
     * 
     * @param session
     * @return maximum of allowed message count
     * @throws MailboxException
     */
    protected abstract long getMaxMessage(MailboxSession session) throws MailboxException;

    
    @Override
    public void event(Event event) {
        String id = event.getSession().getUser().getUserName();
        if (event instanceof Added) {
            Added added = (Added) event;

            long s = 0;
            long c = 0;
            Iterator<Long> uids = added.getUids().iterator();;
            while(uids.hasNext()) {
                long uid = uids.next();
                s += added.getMetaData(uid).getSize();
                c++;
            }
            
            AtomicLong size = sizes.get(id);
            if (size != null) {
                while(true) {
                    long expected = size.get();
                    long newValue = expected + s;
                    if (size.compareAndSet(expected, newValue)) {
                        break;
                    }
                }
            }
            
            AtomicLong count = counts.get(id);
            if (count != null) {
                while(true) {
                    long expected = count.get();
                    long newValue = expected + c;
                    if (count.compareAndSet(expected, newValue)) {
                        break;
                    }
                }
            }
        } else if (event instanceof Expunged) {
            Expunged expunged = (Expunged) event;
            long s = 0;
            long c = 0;
            Iterator<Long> uids = expunged.getUids().iterator();;
            while(uids.hasNext()) {
                long uid = uids.next();
                s += expunged.getMetaData(uid).getSize();
                c++;
            }
            
            AtomicLong size = sizes.get(id);
            if (size != null) {
                while(true) {
                    long expected = size.get();
                    long newValue = expected - s;
                    if (size.compareAndSet(expected, newValue)) {
                        break;
                    }
                }
            }
            
            AtomicLong count = counts.get(id);
            if (count != null) {
                while(true) {
                    long expected = count.get();
                    long newValue = expected - c;
                    if (count.compareAndSet(expected, newValue)) {
                        break;
                    }
                }
            }
        } else if (event instanceof MailboxAdded) {
            counts.putIfAbsent(id, new AtomicLong(0));
            sizes.putIfAbsent(id, new AtomicLong(0));
        }
    }

    /**
     * Get never closed
     * 
     * @return false
     */
    public boolean isClosed() {
        return false;
    }


}
