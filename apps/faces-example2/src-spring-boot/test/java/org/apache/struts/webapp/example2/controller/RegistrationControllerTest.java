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
import org.apache.struts.webapp.example2.domain.MemoryUserDatabase;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link RegistrationController} using MockMvc.
 */
@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    @Test
    void editRegistration_CreateAction_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Create"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void editRegistration_DefaultAction_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void editRegistration_EditAction_WithNoSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void editRegistration_EditAction_WithSession_ShouldPopulateForm() throws Exception {
        MemoryUserDatabase db = new MemoryUserDatabase();
        User user = db.createUser("testuser");
        user.setPassword("password");
        user.setFullName("Test User");
        user.setFromAddress("test@example.com");
        user.setReplyToAddress("reply@example.com");

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
    void saveRegistration_Create_WithValidData_ShouldRedirectToMainMenu() throws Exception {
        when(userDatabase.findUser("newuser")).thenReturn(null);

        MemoryUserDatabase db = new MemoryUserDatabase();
        User createdUser = db.createUser("newuser");
        when(userDatabase.createUser("newuser")).thenReturn(createdUser);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "newuser")
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("password", "password")
                        .param("password2", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));
    }

    @Test
    void saveRegistration_Create_WithDuplicateUsername_ShouldReturnRegistrationWithError() throws Exception {
        MemoryUserDatabase db = new MemoryUserDatabase();
        User existingUser = db.createUser("existing");
        when(userDatabase.findUser("existing")).thenReturn(existingUser);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "existing")
                        .param("fullName", "Test User")
                        .param("fromAddress", "test@example.com")
                        .param("password", "password")
                        .param("password2", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
    }

    @Test
    void saveRegistration_Create_WithMismatchedPasswords_ShouldReturnRegistrationWithError() throws Exception {
        when(userDatabase.findUser("newuser")).thenReturn(null);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "newuser")
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("password", "password1")
                        .param("password2", "password2"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors());
    }

    @Test
    void saveRegistration_Create_WithBlankPassword_ShouldReturnRegistrationWithError() throws Exception {
        when(userDatabase.findUser("newuser")).thenReturn(null);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "newuser")
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("password", "")
                        .param("password2", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "password"));
    }

    @Test
    void saveRegistration_Create_WithBlankUsername_ShouldReturnRegistrationWithError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "")
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("password", "password")
                        .param("password2", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
    }

    @Test
    void saveRegistration_Create_WithInvalidEmail_ShouldReturnRegistrationWithError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "newuser")
                        .param("fullName", "New User")
                        .param("fromAddress", "not-an-email")
                        .param("password", "password")
                        .param("password2", "password"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));
    }

    @Test
    void saveRegistration_Edit_WithNoSession_ShouldRedirectToLogon() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "testuser")
                        .param("fullName", "Test User")
                        .param("fromAddress", "test@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void saveRegistration_Edit_WithSession_ShouldUpdateAndRedirect() throws Exception {
        MemoryUserDatabase db = new MemoryUserDatabase();
        User user = db.createUser("testuser");
        user.setPassword("oldpassword");
        user.setFullName("Old Name");
        user.setFromAddress("old@example.com");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "testuser")
                        .param("fullName", "Updated Name")
                        .param("fromAddress", "updated@example.com")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));
    }

    @Test
    void saveRegistration_Edit_WithNewPassword_ShouldUpdatePassword() throws Exception {
        MemoryUserDatabase db = new MemoryUserDatabase();
        User user = db.createUser("testuser");
        user.setPassword("oldpassword");
        user.setFullName("Test User");
        user.setFromAddress("test@example.com");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "testuser")
                        .param("fullName", "Test User")
                        .param("fromAddress", "test@example.com")
                        .param("password", "newpassword")
                        .param("password2", "newpassword")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));
    }
}
