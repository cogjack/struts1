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
package org.apache.struts.webapp.example2.springmvc.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * Form bean for the subscription page. This form has the following fields:
 * <ul>
 * <li><b>action</b> - The maintenance action we are performing (Create, Delete, or Edit).</li>
 * <li><b>host</b> - The mail host for this subscription. [REQUIRED]</li>
 * <li><b>password</b> - The password for this subscription.</li>
 * <li><b>type</b> - The subscription type (imap, pop3) for this subscription. [REQUIRED]</li>
 * <li><b>username</b> - The username of this subscription. [REQUIRED]</li>
 * <li><b>autoConnect</b> - Whether to auto-connect at startup.</li>
 * </ul>
 */
public class SubscriptionForm {

    private String action = "Create";

    @NotBlank(message = "{error.host.required}")
    private String host;

    private String password;

    @NotBlank(message = "{error.type.required}")
    @Pattern(regexp = "^(imap|pop3)$", message = "{error.type.invalid}")
    private String type;

    @NotBlank(message = "{error.username.required}")
    private String username;

    private boolean autoConnect;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }
}
