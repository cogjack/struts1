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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    private MockHttpSession sessionWithUser() {
        MockHttpSession session = new MockHttpSession();
        User user = userDatabase.findUser("user");
        session.setAttribute(Constants.USER_KEY, user);
        return session;
    }

    @Test
    void showForm_NewSubscription_ShouldReturnSubscriptionView() throws Exception {
        mockMvc.perform(get("/editSubscription")
                .session(sessionWithUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"))
                .andExpect(model().attributeExists("serverTypes"));
    }

    @Test
    void showForm_NewSubscription_ShouldHaveCreateAction() throws Exception {
        mockMvc.perform(get("/editSubscription")
                .session(sessionWithUser()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("subscriptionForm"));
    }

    @Test
    void showForm_EditExistingSubscription_ShouldPopulateForm() throws Exception {
        mockMvc.perform(get("/editSubscription")
                .param("action", "Edit")
                .param("host", "mail.yahoo.com")
                .session(sessionWithUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeExists("subscriptionForm"));
    }

    @Test
    void showForm_EditNonExistentSubscription_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/editSubscription")
                .param("action", "Edit")
                .param("host", "nonexistent.host.com")
                .session(sessionWithUser()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void showForm_DeleteAction_ShouldReturnSubscriptionView() throws Exception {
        mockMvc.perform(get("/editSubscription")
                .param("action", "Delete")
                .param("host", "mail.yahoo.com")
                .session(sessionWithUser()))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"));
    }

    @Test
    void showForm_NoUserInSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editSubscription"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logon"));
    }

    @Test
    void saveSubscription_CreateNew_ShouldRedirect() throws Exception {
        String newHost = "mail.newhost_" + System.currentTimeMillis() + ".com";
        MockHttpSession session = sessionWithUser();

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", newHost)
                .param("username", "newuser")
                .param("password", "newpass")
                .param("type", "imap")
                .param("autoConnect", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        User user = (User) session.getAttribute(Constants.USER_KEY);
        Subscription created = user.findSubscription(newHost);
        assertThat(created).isNotNull();
        assertThat(created.getUsername()).isEqualTo("newuser");
        assertThat(created.getPassword()).isEqualTo("newpass");
        assertThat(created.getType()).isEqualTo("imap");
        assertThat(created.getAutoConnect()).isTrue();
    }

    @Test
    void saveSubscription_EditExisting_ShouldUpdateAndRedirect() throws Exception {
        String editHost = "mail.edit_" + System.currentTimeMillis() + ".com";
        User user = userDatabase.findUser("user");
        Subscription sub = user.createSubscription(editHost);
        sub.setType("imap");
        sub.setUsername("olduser");
        sub.setPassword("oldpass");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, sub);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", editHost)
                .param("username", "updateduser")
                .param("password", "updatedpass")
                .param("type", "pop3")
                .param("autoConnect", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        Subscription updated = user.findSubscription(editHost);
        assertThat(updated).isNotNull();
        assertThat(updated.getUsername()).isEqualTo("updateduser");
        assertThat(updated.getPassword()).isEqualTo("updatedpass");
        assertThat(updated.getType()).isEqualTo("pop3");
    }

    @Test
    void saveSubscription_Delete_ShouldRemoveAndRedirect() throws Exception {
        String deleteHost = "mail.delete_" + System.currentTimeMillis() + ".com";
        User user = userDatabase.findUser("user");
        Subscription sub = user.createSubscription(deleteHost);
        sub.setType("imap");
        sub.setUsername("deluser");
        sub.setPassword("delpass");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, sub);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Delete")
                .param("host", deleteHost)
                .param("username", "deluser")
                .param("password", "delpass")
                .param("type", "imap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        assertThat(user.findSubscription(deleteHost)).isNull();
    }

    @Test
    void saveSubscription_BlankHost_ShouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .session(sessionWithUser())
                .param("action", "Create")
                .param("host", "")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("type", "imap"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void saveSubscription_BlankUsername_ShouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .session(sessionWithUser())
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "")
                .param("password", "testpass")
                .param("type", "imap"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "username"));
    }

    @Test
    void saveSubscription_NoUserInSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("type", "imap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logon"));
    }

    @Test
    void saveSubscription_InvalidType_ShouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .session(sessionWithUser())
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "testuser")
                .param("password", "testpass")
                .param("type", "smtp"))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));
    }
}
