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

/**
 * Concrete implementation of Subscription for an in-memory database backed by an XML data file.
 */
public final class MemorySubscription implements Subscription {

    private final String host;
    private final MemoryUser user;
    private boolean autoConnect = false;
    private String password = null;
    private String type = "imap";
    private String username = null;

    /**
     * Construct a new Subscription associated with the specified User.
     *
     * @param user The user with which we are associated
     * @param host The mail host for this subscription
     */
    public MemorySubscription(MemoryUser user, String host) {
        this.user = user;
        this.host = host;
    }

    @Override
    public boolean getAutoConnect() {
        return this.autoConnect;
    }

    @Override
    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    @Override
    public String getHost() {
        return this.host;
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
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<subscription host=\"");
        sb.append(host);
        sb.append("\" autoConnect=\"");
        sb.append(autoConnect);
        sb.append("\"");
        if (password != null) {
            sb.append(" password=\"");
            sb.append(password);
            sb.append("\"");
        }
        if (type != null) {
            sb.append(" type=\"");
            sb.append(type);
            sb.append("\"");
        }
        if (username != null) {
            sb.append(" username=\"");
            sb.append(username);
            sb.append("\"");
        }
        sb.append(">");
        return sb.toString();
    }
}
