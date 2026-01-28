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
package org.apache.struts.webapp.example2.springmvc.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.Subscription;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.apache.struts.webapp.example2.springmvc.config.DatabaseConfiguration.ServerType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserDatabase userDatabase;

    @Mock
    private User user;

    @Mock
    private Subscription subscription;

    private List<ServerType> serverTypes;
    private MockHttpSession session;

    @Before
    public void setUp() {
        serverTypes = Arrays.asList(
            new ServerType("IMAP Protocol", "imap"),
            new ServerType("POP3 Protocol", "pop3")
        );

        SubscriptionController controller = new SubscriptionController(userDatabase, serverTypes);

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setViewResolvers(viewResolver)
            .build();

        session = new MockHttpSession();

        when(user.getUsername()).thenReturn("testuser");
    }

    @Test
    public void editSubscription_WithNoUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editSubscription"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logon"));
    }

    @Test
    public void editSubscription_CreateAction_ShouldShowEmptyForm() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .session(session)
                .param("action", "Create"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeExists("subscriptionForm"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attributeExists("serverTypes"));
    }

    @Test
    public void editSubscription_EditAction_WithExistingSubscription_ShouldPopulateForm() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        when(user.findSubscription("mail.example.com")).thenReturn(subscription);
        when(subscription.getHost()).thenReturn("mail.example.com");
        when(subscription.getUsername()).thenReturn("mailuser");
        when(subscription.getPassword()).thenReturn("mailpass");
        when(subscription.getType()).thenReturn("imap");
        when(subscription.getAutoConnect()).thenReturn(true);

        mockMvc.perform(get("/editSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", "mail.example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeExists("subscriptionForm"));
    }

    @Test
    public void editSubscription_EditAction_WithNonExistingSubscription_ShouldRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        when(user.findSubscription("nonexistent.com")).thenReturn(null);

        mockMvc.perform(get("/editSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", "nonexistent.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));
    }

    @Test
    public void editSubscription_DeleteAction_WithExistingSubscription_ShouldShowForm() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        when(user.findSubscription("mail.example.com")).thenReturn(subscription);
        when(subscription.getHost()).thenReturn("mail.example.com");
        when(subscription.getUsername()).thenReturn("mailuser");
        when(subscription.getPassword()).thenReturn("mailpass");
        when(subscription.getType()).thenReturn("pop3");
        when(subscription.getAutoConnect()).thenReturn(false);

        mockMvc.perform(get("/editSubscription")
                .session(session)
                .param("action", "Delete")
                .param("host", "mail.example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"));
    }

    @Test
    public void saveSubscription_WithNoUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveSubscription")
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/logon"));
    }

    @Test
    public void saveSubscription_WithCancel_ShouldRedirectToRegistration() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap")
                .param("cancel", "cancel"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(userDatabase, never()).save();
    }

    @Test
    public void saveSubscription_CreateAction_WithValidData_ShouldCreateAndRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        Subscription newSubscription = mock(Subscription.class);
        when(user.createSubscription("mail.example.com")).thenReturn(newSubscription);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(user).createSubscription("mail.example.com");
        verify(newSubscription).setUsername("mailuser");
        verify(newSubscription).setPassword("mailpass");
        verify(newSubscription).setType("imap");
        verify(userDatabase).save();
    }

    @Test
    public void saveSubscription_CreateAction_WithBlankHost_ShouldShowValidationError() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));

        verify(user, never()).createSubscription(anyString());
    }

    @Test
    public void saveSubscription_CreateAction_WithBlankUsername_ShouldShowValidationError() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "username"));

        verify(user, never()).createSubscription(anyString());
    }

    @Test
    public void saveSubscription_CreateAction_WithInvalidType_ShouldShowValidationError() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "invalid"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));

        verify(user, never()).createSubscription(anyString());
    }

    @Test
    public void saveSubscription_CreateAction_WithBlankType_ShouldShowValidationError() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "type"));

        verify(user, never()).createSubscription(anyString());
    }

    @Test
    public void saveSubscription_EditAction_WithValidData_ShouldUpdateAndRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .param("username", "newuser")
                .param("password", "newpass")
                .param("type", "pop3")
                .param("autoConnect", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(subscription).setUsername("newuser");
        verify(subscription).setPassword("newpass");
        verify(subscription).setType("pop3");
        verify(subscription).setAutoConnect(true);
        verify(userDatabase).save();
    }

    @Test
    public void saveSubscription_EditAction_WithNoSubscriptionInSession_ShouldRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Edit")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(userDatabase, never()).save();
    }

    @Test
    public void saveSubscription_DeleteAction_ShouldDeleteAndRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

        when(subscription.getHost()).thenReturn("mail.example.com");

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Delete")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(user).removeSubscription(subscription);
        verify(userDatabase).save();
    }

    @Test
    public void saveSubscription_DeleteAction_WithNoSubscriptionInSession_ShouldRedirect() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Delete")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editRegistration"));

        verify(user, never()).removeSubscription(subscription);
    }

    @Test
    public void saveSubscription_CreateAction_WithDuplicateHost_ShouldShowError() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        when(user.createSubscription("mail.example.com"))
            .thenThrow(new IllegalArgumentException("Host already exists"));

        mockMvc.perform(post("/saveSubscription")
                .session(session)
                .param("action", "Create")
                .param("host", "mail.example.com")
                .param("username", "mailuser")
                .param("password", "mailpass")
                .param("type", "imap"))
            .andExpect(status().isOk())
            .andExpect(view().name("subscription"))
            .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
    }

    @Test
    public void serverTypes_ShouldBeAvailableInModel() throws Exception {
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editSubscription")
                .session(session)
                .param("action", "Create"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("serverTypes", serverTypes));
    }
}
