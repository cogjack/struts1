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
package org.apache.struts.webapp.example2.controller;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link SubscriptionController} using MockMvc
 * with full Spring Boot context to support List&lt;LabelValueBean&gt; injection.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    private User user;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        String uniqueName = "subctrltest" + System.currentTimeMillis();
        user = userDatabase.createUser(uniqueName);
        user.setPassword("password");
        user.setFullName("Test User");
        user.setFromAddress("test@example.com");

        session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
    }

    @Test
    void editSubscription_WithNoSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editSubscription"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void editSubscription_CreateAction_ShouldReturnSubscriptionView() throws Exception {
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Create")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void editSubscription_EditAction_WithExistingHost_ShouldPopulateForm() throws Exception {
        Subscription sub = user.createSubscription("mail.example.com");
        sub.setUsername("mailuser");
        sub.setPassword("mailpass");
        sub.setType("imap");
        sub.setAutoConnect(false);

        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.example.com")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void editSubscription_EditAction_WithMissingHost_ShouldRedirectToRegistration() throws Exception {
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "nonexistent.example.com")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_WithNoSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "mail.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void saveSubscription_Cancel_ShouldRedirectToRegistration() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("cancel", "Cancel")
                        .param("host", "mail.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_Create_WithValidData_ShouldRedirectToRegistration() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "newhost-" + System.currentTimeMillis() + ".example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_Create_WithBlankHost_ShouldReturnSubscriptionWithError() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void saveSubscription_Create_WithInvalidType_ShouldReturnSubscriptionWithError() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "mail.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "smtp")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));
    }

    @Test
    void saveSubscription_Create_WithDuplicateHost_ShouldReturnSubscriptionWithError() throws Exception {
        user.createSubscription("duplicate.example.com");

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "duplicate.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void saveSubscription_Delete_WithSubscriptionInSession_ShouldRedirectToRegistration() throws Exception {
        Subscription sub = user.createSubscription("todelete.example.com");
        sub.setUsername("mailuser");
        sub.setPassword("mailpass");
        sub.setType("imap");

        session.setAttribute(Constants.SUBSCRIPTION_KEY, sub);

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Delete")
                        .param("host", "todelete.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_Delete_WithNoSubscriptionInSession_ShouldRedirectToRegistration() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Delete")
                        .param("host", "mail.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_Edit_WithSubscriptionInSession_ShouldRedirectToRegistration() throws Exception {
        Subscription sub = user.createSubscription("toedit.example.com");
        sub.setUsername("olduser");
        sub.setPassword("oldpass");
        sub.setType("pop3");

        session.setAttribute(Constants.SUBSCRIPTION_KEY, sub);

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("host", "toedit.example.com")
                        .param("username", "newuser")
                        .param("password", "newpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_Edit_WithNoSubscriptionInSession_ShouldRedirectToRegistration() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.example.com")
                        .param("username", "mailuser")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }
}
