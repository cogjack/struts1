/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.struts.webapp.example2.domain;

import java.util.HashMap;
import java.util.Map;

public final class MemoryUser implements User {

    private final MemoryUserDatabase database;
    private final String username;
    private final Map<String, Subscription> subscriptions = new HashMap<>();
    private String fromAddress = null;
    private String fullName = null;
    private String password = null;
    private String replyToAddress = null;

    public MemoryUser(MemoryUserDatabase database, String username) {
        this.database = database;
        this.username = username;
    }

    @Override
    public UserDatabase getDatabase() {
        return this.database;
    }

    @Override
    public String getFromAddress() {
        return this.fromAddress;
    }

    @Override
    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Override
    public String getFullName() {
        return this.fullName;
    }

    @Override
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getReplyToAddress() {
        return this.replyToAddress;
    }

    @Override
    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    @Override
    public Subscription[] getSubscriptions() {
        synchronized (subscriptions) {
            Subscription[] results = new Subscription[subscriptions.size()];
            return subscriptions.values().toArray(results);
        }
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public Subscription createSubscription(String host) {
        synchronized (subscriptions) {
            if (subscriptions.get(host) != null) {
                throw new IllegalArgumentException("Duplicate host '" + host
                        + "' for user '" + username + "'");
            }
            MemorySubscription subscription = new MemorySubscription(this, host);
            subscriptions.put(host, subscription);
            return subscription;
        }
    }

    @Override
    public Subscription findSubscription(String host) {
        synchronized (subscriptions) {
            return subscriptions.get(host);
        }
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        if (this != subscription.getUser()) {
            throw new IllegalArgumentException("Subscription not associated with this user");
        }
        synchronized (subscriptions) {
            subscriptions.remove(subscription.getHost());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<user username=\"");
        sb.append(username);
        sb.append("\"");
        if (fromAddress != null) {
            sb.append(" fromAddress=\"");
            sb.append(fromAddress);
            sb.append("\"");
        }
        if (fullName != null) {
            sb.append(" fullName=\"");
            sb.append(fullName);
            sb.append("\"");
        }
        if (password != null) {
            sb.append(" password=\"");
            sb.append(password);
            sb.append("\"");
        }
        if (replyToAddress != null) {
            sb.append(" replyToAddress=\"");
            sb.append(replyToAddress);
            sb.append("\"");
        }
        sb.append(">");
        return sb.toString();
    }
}
