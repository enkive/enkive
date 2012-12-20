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

import org.apache.james.mailbox.model.Quota;

public final class QuotaImpl implements Quota{
    
    private long max;
    private long used;

    private final static Quota UNLIMITED_QUOTA = new QuotaImpl(UNKNOWN, UNLIMITED);
    
    private QuotaImpl(long used, long max) {
        
    }

    @Override
    public long getMax() {
        return max;
    }

    @Override
    public long getUsed() {
        return used;
    }
    
    /**
     * Return a {@link Quota} which in fact is unlimited
     * 
     * @return unlimited
     */
    public static Quota unlimited() {
        return UNLIMITED_QUOTA;
    }
    
    /**
     * Return a {@link Quota} for the given values
     * 
     * @param max
     * @param used
     * @return quota
     */
    public static Quota quota(long max , long used) {
        return new QuotaImpl(used, max);
    }

}
