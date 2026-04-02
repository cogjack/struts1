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
package org.apache.struts.webapp.example2.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.apache.struts.webapp.example2.validation.PasswordMatch;

/**
 * Form backing object for the user registration page.
 * Replaces the Struts RegistrationForm (ValidatorForm) with a typed POJO
 * using Bean Validation annotations.
 *
 * <p>Fields:</p>
 * <ul>
 *   <li><b>action</b> - The maintenance action (Create or Edit)</li>
 *   <li><b>username</b> - The registered username [REQUIRED]</li>
 *   <li><b>fullName</b> - The full name of the user [REQUIRED]</li>
 *   <li><b>fromAddress</b> - The sender email address [REQUIRED, valid email]</li>
 *   <li><b>replyToAddress</b> - The reply-to email address [valid email if provided]</li>
 *   <li><b>password</b> - The password (required on Create, optional on Edit)</li>
 *   <li><b>password2</b> - The confirmation password (must match password)</li>
 * </ul>
 */
@PasswordMatch
public class RegistrationForm {

    private String action = "Create";

    @NotBlank(message = "{error.username.required}")
    private String username;

    @NotBlank(message = "{error.fullName.required}")
    private String fullName;

    @NotBlank(message = "{error.fromAddress.required}")
    @Email(message = "{error.fromAddress.format}")
    private String fromAddress;

    @Email(message = "{error.replyToAddress.format}")
    private String replyToAddress;

    private String password;

    private String password2;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getReplyToAddress() {
        return replyToAddress;
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }
}
