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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    @Test
    void editRegistration_CreateMode_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration")
                .param("action", "Create"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void editRegistration_CreateMode_DefaultAction_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void editRegistration_EditMode_WithLoggedInUser_ShouldPopulateForm() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getFullName()).thenReturn("Test User");
        when(user.getFromAddress()).thenReturn("test@example.com");
        when(user.getReplyToAddress()).thenReturn("reply@example.com");
        when(user.getSubscriptions()).thenReturn(new Subscription[0]);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editRegistration")
                .param("action", "Edit")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"))
            .andExpect(model().attributeExists("user"));
    }

    @Test
    void editRegistration_EditMode_WithoutLoggedInUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editRegistration")
                .param("action", "Edit"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void saveRegistration_CreateMode_WithValidData_ShouldCreateUserAndRedirect() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("newuser");
        when(user.getPassword()).thenReturn("password123");
        when(userDatabase.findUser("newuser")).thenReturn(null);
        when(userDatabase.createUser("newuser")).thenReturn(user);

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", "new@example.com")
                .param("replyToAddress", "reply@example.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));

        verify(userDatabase).createUser("newuser");
        verify(userDatabase).save();
    }

    @Test
    void saveRegistration_CreateMode_WithExistingUsername_ShouldReturnError() throws Exception {
        User existingUser = mock(User.class);
        when(userDatabase.findUser("existinguser")).thenReturn(existingUser);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "existinguser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithMissingPassword_ShouldReturnError() throws Exception {
        when(userDatabase.findUser("newuser")).thenReturn(null);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "")
                .param("password2", "")
                .param("fullName", "New User")
                .param("fromAddress", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "password"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "password2"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithPasswordMismatch_ShouldReturnError() throws Exception {
        when(userDatabase.findUser("newuser")).thenReturn(null);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "differentpassword")
                .param("fullName", "New User")
                .param("fromAddress", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "password2"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithBlankUsername_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithBlankFullName_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "")
                .param("fromAddress", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "fullName"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithBlankFromAddress_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithInvalidFromAddressEmail_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", "not-an-email"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_CreateMode_WithInvalidReplyToAddressEmail_ShouldReturnError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Create")
                .param("username", "newuser")
                .param("password", "password123")
                .param("password2", "password123")
                .param("fullName", "New User")
                .param("fromAddress", "valid@example.com")
                .param("replyToAddress", "not-an-email"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeHasFieldErrors("registrationForm", "replyToAddress"));

        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_EditMode_WithLoggedInUser_ShouldUpdateAndRedirect() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn("oldpassword");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Edit")
                .param("username", "testuser")
                .param("fullName", "Updated Name")
                .param("fromAddress", "updated@example.com")
                .param("replyToAddress", "reply@example.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));

        verify(userDatabase).save();
        verify(userDatabase, never()).createUser(anyString());
    }

    @Test
    void saveRegistration_EditMode_WithoutLoggedInUser_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                .param("action", "Edit")
                .param("username", "testuser")
                .param("fullName", "Updated Name")
                .param("fromAddress", "updated@example.com"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/editLogon"));

        verify(userDatabase, never()).save();
    }

    @Test
    void saveRegistration_EditMode_WithNewPassword_ShouldUpdatePassword() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn("oldpassword");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Edit")
                .param("username", "testuser")
                .param("password", "newpassword")
                .param("password2", "newpassword")
                .param("fullName", "Test User")
                .param("fromAddress", "test@example.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));

        verify(userDatabase).save();
        verify(user).setPassword("newpassword");
    }

    @Test
    void saveRegistration_EditMode_WithoutPassword_ShouldKeepOldPassword() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn("oldpassword");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Edit")
                .param("username", "testuser")
                .param("fullName", "Test User")
                .param("fromAddress", "test@example.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));

        verify(userDatabase).save();
        verify(user).setPassword("oldpassword");
    }
}
