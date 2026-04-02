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
import org.apache.struts.webapp.example2.TestDatabaseConfiguration;
import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 5.3 - Full Request Integration Tests for Subscription.
 * Tests the complete subscription CRUD flow requiring authenticated sessions.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    private MockHttpSession authenticatedSession;

    @BeforeEach
    void loginUser() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andReturn();
        authenticatedSession = (MockHttpSession) loginResult.getRequest().getSession();
    }

    // --- GET /editSubscription ---

    @Test
    void getEditSubscription_WithoutSession_RedirectsToLogon() throws Exception {
        mockMvc.perform(get("/editSubscription").param("action", "Create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void getEditSubscription_CreateAction_ReturnsForm() throws Exception {
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Create")
                        .session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"))
                .andExpect(model().attributeExists("serverTypes"));
    }

    @Test
    void getEditSubscription_EditExistingSubscription_ReturnsPopulatedForm() throws Exception {
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.yahoo.com")
                        .session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"));
    }

    @Test
    void getEditSubscription_NonExistentHost_RedirectsToRegistration() throws Exception {
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "nonexistent.host.com")
                        .session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    // --- POST /saveSubscription (Create) ---

    @Test
    void postSaveSubscription_CreateNew_RedirectsToRegistration() throws Exception {
        String uniqueHost = "mail.new" + System.currentTimeMillis() + ".com";

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", uniqueHost)
                        .param("username", "newsubuser")
                        .param("password", "subpass")
                        .param("type", "imap")
                        .session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was created
        User user = userDatabase.findUser("user");
        Subscription sub = user.findSubscription(uniqueHost);
        assertThat(sub).isNotNull();
        assertThat(sub.getUsername()).isEqualTo("newsubuser");
        assertThat(sub.getType()).isEqualTo("imap");
    }

    @Test
    void postSaveSubscription_DuplicateHost_ReturnsError() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "mail.yahoo.com")
                        .param("username", "dupuser")
                        .param("password", "duppass")
                        .param("type", "imap")
                        .session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void postSaveSubscription_MissingRequiredFields_ReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "")
                        .param("username", "")
                        .param("password", "")
                        .param("type", "")
                        .session(authenticatedSession))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().hasErrors());
    }

    @Test
    void postSaveSubscription_WithoutSession_RedirectsToLogon() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "mail.test.com")
                        .param("username", "testuser")
                        .param("password", "testpass")
                        .param("type", "imap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    // --- POST /saveSubscription (Edit) ---

    @Test
    void postSaveSubscription_EditExisting_UpdatesAndRedirects() throws Exception {
        // First navigate to edit the subscription to set it in session
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.yahoo.com")
                        .session(authenticatedSession))
                .andExpect(status().isOk());

        // Then save updated subscription
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.yahoo.com")
                        .param("username", "updateduser")
                        .param("password", "updatedpass")
                        .param("type", "pop3")
                        .session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was updated
        User user = userDatabase.findUser("user");
        Subscription sub = user.findSubscription("mail.yahoo.com");
        assertThat(sub.getUsername()).isEqualTo("updateduser");
        assertThat(sub.getType()).isEqualTo("pop3");
    }

    // --- POST /saveSubscription (Delete) ---

    @Test
    void postSaveSubscription_Delete_RemovesSubscription() throws Exception {
        // Create a subscription to delete
        String deleteHost = "mail.delete" + System.currentTimeMillis() + ".com";
        User user = userDatabase.findUser("user");
        Subscription sub = user.createSubscription(deleteHost);
        sub.setType("imap");
        sub.setUsername("deluser");
        sub.setPassword("delpass");

        // Navigate to edit the subscription to set it in session
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Delete")
                        .param("host", deleteHost)
                        .session(authenticatedSession))
                .andExpect(status().isOk());

        // Delete the subscription
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Delete")
                        .session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was removed
        assertThat(user.findSubscription(deleteHost)).isNull();
    }

    // --- POST /saveSubscription (Cancel) ---

    @Test
    void postSaveSubscription_Cancel_RedirectsWithoutSaving() throws Exception {
        // Navigate to edit subscription
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", "mail.hotmail.com")
                        .session(authenticatedSession))
                .andExpect(status().isOk());

        // Cancel
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("cancel", "Cancel")
                        .param("host", "mail.hotmail.com")
                        .param("username", "should-not-save")
                        .param("password", "should-not-save")
                        .param("type", "imap")
                        .session(authenticatedSession))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was NOT updated
        User user = userDatabase.findUser("user");
        Subscription sub = user.findSubscription("mail.hotmail.com");
        assertThat(sub.getUsername()).isNotEqualTo("should-not-save");
    }
}
