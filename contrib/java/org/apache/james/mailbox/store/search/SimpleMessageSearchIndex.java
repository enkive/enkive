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
package org.apache.james.mailbox.store.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mailbox.model.SearchQuery;
import org.apache.james.mailbox.model.SearchQuery.Criterion;
import org.apache.james.mailbox.model.SearchQuery.NumericRange;
import org.apache.james.mailbox.model.SearchQuery.UidCriterion;
import org.apache.james.mailbox.store.mail.MessageMapper;
import org.apache.james.mailbox.store.mail.MessageMapper.FetchType;
import org.apache.james.mailbox.store.mail.MessageMapperFactory;
import org.apache.james.mailbox.store.mail.model.Mailbox;
import org.apache.james.mailbox.store.mail.model.Message;

/**
 * {@link MessageSearchIndex} which just fetch {@link Message}'s from the {@link MessageMapper} and use {@link MessageSearcher}
 * to match them against the {@link SearchQuery}.
 * 
 * This works with every implementation but is SLOW.
 * 
 *
 * @param <Id>
 */
public class SimpleMessageSearchIndex<Id> implements MessageSearchIndex<Id>{

    private final MessageMapperFactory<Id> factory;
    public SimpleMessageSearchIndex(MessageMapperFactory<Id> factory) {
        this.factory = factory;
    }
    
    @Override
    public Iterator<Long> search(MailboxSession session, Mailbox<Id> mailbox, SearchQuery query) throws MailboxException {
        List<Criterion> crits = query.getCriterias();
        MessageMapper<Id> mapper = factory.getMessageMapper(session);
        
        // Ok we only search for a range so we can optimize the call
        if (crits.size() == 1  && crits.get(0) instanceof UidCriterion) {
            final List<Long> uids = new ArrayList<Long>();
            UidCriterion uidCrit = (UidCriterion) crits.get(0);
            NumericRange[] ranges = uidCrit.getOperator().getRange();
            for (int i = 0; i < ranges.length; i++) {
                NumericRange r = ranges[i];
                Iterator<Message<Id>> messages = mapper.findInMailbox(mailbox, MessageRange.range(r.getLowValue(), r.getHighValue()), FetchType.Metadata, -1);
                
                while(messages.hasNext()) {
                    long uid = messages.next().getUid();
                    if (uids.contains(uid) == false) {
                        uids.add(uid);
                    }
                }
            }
            Collections.sort(uids);
            return uids.iterator();
            
           
        } else {
            
            final List<Message<Id>> hits = new ArrayList<Message<Id>>();

            Iterator<Message<Id>> messages = mapper.findInMailbox(mailbox, MessageRange.all(), FetchType.Full, -1);
            while(messages.hasNext()) {
            	Message<Id> m = messages.next();
                if (hits.contains(m) == false) {
                    hits.add(m);
                }
            }
            Collections.sort(hits);
            
            Iterator<Message<?>> it = new Iterator<Message<?>>() {
                final Iterator<Message<Id>> it = hits.iterator();
                public boolean hasNext() {
                    return it.hasNext();
                }

                public Message<?> next() {
                    return it.next();
                }

                public void remove() {
                    it.remove();
                }
                
            };
            
            if (session == null) {
                return new MessageSearches(it, query).iterator();
            } else {
                return new MessageSearches(it, query, session.getLog()).iterator();
            }
        }
    }

}
