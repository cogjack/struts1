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

import java.util.List;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.Subscription;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.apache.struts.webapp.example2.config.DatabaseConfiguration.LabelValueBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    @MockBean
    private List<LabelValueBean> serverTypes;

    @Test
    void editSubscription_WithNoUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editSubscription"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void editSubscription_CreateAction_ShouldShowEmptyForm() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .param("action", "Create")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeExists("subscriptionForm"))
            .andExpect(model().attributeExists("user"));
    }

    @Test
    void editSubscription_EditAction_WithExistingSubscription_ShouldPopulateForm() throws Exception {
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.findSubscription("mail.example.com")).thenReturn(subscription);
        when(subscription.getHost()).thenReturn("mail.example.com");
        when(subscription.getUsername()).thenReturn("mailuser");
        when(subscription.getPassword()).thenReturn("mailpass");
        when(subscription.getType()).thenReturn("imap");
        when(subscription.getAutoConnect()).thenReturn(true);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeExists("subscriptionForm"));
    }

    @Test
    void editSubscription_EditAction_WithNonExistingSubscription_ShouldRedirect() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.findSubscription("nonexistent.com")).thenReturn(null);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .param("action", "Edit")
                .param("host", "nonexistent.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void editSubscription_DeleteAction_WithExistingSubscription_ShouldShowForm() throws Exception {
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.findSubscription("mail.example.com")).thenReturn(subscription);
        when(subscription.getHost()).thenReturn("mail.example.com");
        when(subscription.getUsername()).thenReturn("mailuser");
        when(subscription.getPassword()).thenReturn("mailpass");
        when(subscription.getType()).thenReturn("pop3");
        when(subscription.getAutoConnect()).thenReturn(false);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .param("action", "Delete")
                .param("host", "mail.example.com")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"));
    }

    @Test
    void saveSubscription_WithNoUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void saveSubscription_WithCancel_ShouldRedirectToRegistration() throws Exception {
        User user = mock(User.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap")
                .param("cancel", "cancel")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_CreateAction_WithValidData_ShouldCreateAndRedirect() throws Exception {
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);
        when(user.createSubscription("mail.example.com")).thenReturn(subscription);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(subscription).setUsername("mailuser");
        verify(subscription).setPassword("mailpass");
        verify(subscription).setType("imap");
        verify(userDatabase).save();
    }

    @Test
    void saveSubscription_CreateAction_WithDuplicateHost_ShouldShowError() throws Exception {
        User user = mock(User.class);
        when(user.createSubscription("mail.example.com")).thenThrow(new IllegalArgumentException("Duplicate host"));

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void saveSubscription_CreateAction_WithBlankHost_ShouldShowValidationError() throws Exception {
        User user = mock(User.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "")
                .param("username", "mailuser")
                .param("type", "imap")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    void saveSubscription_CreateAction_WithBlankUsername_ShouldShowValidationError() throws Exception {
        User user = mock(User.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "")
                .param("type", "imap")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "username"));
    }

    @Test
    void saveSubscription_CreateAction_WithBlankType_ShouldShowValidationError() throws Exception {
        User user = mock(User.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));
    }

    @Test
    void saveSubscription_CreateAction_WithInvalidType_ShouldShowValidationError() throws Exception {
        User user = mock(User.class);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "invalid")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));
    }

    @Test
    void saveSubscription_EditAction_WithValidData_ShouldUpdateAndRedirect() throws Exception {
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .param("username", "newuser")
                .param("password", "newpass")
                .param("type", "pop3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(subscription).setUsername("newuser");
        verify(subscription).setPassword("newpass");
        verify(subscription).setType("pop3");
        verify(userDatabase).save();
    }

    @Test
    void saveSubscription_EditAction_WithNoSubscriptionInSession_ShouldRedirect() throws Exception {
        User user = mock(User.class);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    void saveSubscription_DeleteAction_ShouldDeleteAndRedirect() throws Exception {
        User user = mock(User.class);
        Subscription subscription = mock(Subscription.class);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Delete")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(user).removeSubscription(subscription);
        verify(userDatabase).save();
    }

    @Test
    void saveSubscription_DeleteAction_WithNoSubscriptionInSession_ShouldRedirect() throws Exception {
        User user = mock(User.class);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .param("action", "Delete")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("type", "imap")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(user, never()).removeSubscription(any());
    }

    @Test
    void serverTypes_ShouldBeAvailableInModel() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .param("action", "Create")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("serverTypes"));
    }
}
