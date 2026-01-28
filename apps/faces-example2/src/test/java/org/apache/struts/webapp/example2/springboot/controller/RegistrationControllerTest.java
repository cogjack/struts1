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
package org.apache.struts.webapp.example2.springboot.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.Subscription;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegistrationController.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    private MockHttpSession session;
    private User mockUser;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        mockUser = new User() {
            private String username = "testuser";
            private String password = "testpass";
            private String fullName = "Test User";
            private String fromAddress = "test@example.com";
            private String replyToAddress = "reply@example.com";

            @Override
            public UserDatabase getDatabase() {
                return userDatabase;
            }

            @Override
            public String getFromAddress() {
                return fromAddress;
            }

            @Override
            public void setFromAddress(String fromAddress) {
                this.fromAddress = fromAddress;
            }

            @Override
            public String getFullName() {
                return fullName;
            }

            @Override
            public void setFullName(String fullName) {
                this.fullName = fullName;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public void setPassword(String password) {
                this.password = password;
            }

            @Override
            public String getReplyToAddress() {
                return replyToAddress;
            }

            @Override
            public void setReplyToAddress(String replyToAddress) {
                this.replyToAddress = replyToAddress;
            }

            @Override
            public Subscription[] getSubscriptions() {
                return new Subscription[0];
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public Subscription createSubscription(String host) {
                return null;
            }

            @Override
            public Subscription findSubscription(String host) {
                return null;
            }

            @Override
            public void removeSubscription(Subscription subscription) {
            }
        };
    }

    @Test
    void editRegistration_CreateMode_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration")
                .param("action", "Create"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"))
            .andExpect(model().attribute("registrationForm",
                org.hamcrest.Matchers.hasProperty("action", org.hamcrest.Matchers.equalTo("Create"))));
    }

    @Test
    void editRegistration_CreateMode_DefaultAction_ShouldReturnRegistrationView() throws Exception {
        mockMvc.perform(get("/editRegistration"))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"))
            .andExpect(model().attribute("registrationForm",
                org.hamcrest.Matchers.hasProperty("action", org.hamcrest.Matchers.equalTo("Create"))));
    }

    @Test
    void editRegistration_EditMode_WithLoggedInUser_ShouldPopulateForm() throws Exception {
        session.setAttribute(Constants.USER_KEY, mockUser);

        mockMvc.perform(get("/editRegistration")
                .param("action", "Edit")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("registration"))
            .andExpect(model().attributeExists("registrationForm"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("registrationForm",
                org.hamcrest.Matchers.hasProperty("username", org.hamcrest.Matchers.equalTo("testuser"))))
            .andExpect(model().attribute("registrationForm",
                org.hamcrest.Matchers.hasProperty("fullName", org.hamcrest.Matchers.equalTo("Test User"))))
            .andExpect(model().attribute("registrationForm",
                org.hamcrest.Matchers.hasProperty("fromAddress", org.hamcrest.Matchers.equalTo("test@example.com"))));
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
        when(userDatabase.findUser("newuser")).thenReturn(null);
        when(userDatabase.createUser("newuser")).thenReturn(mockUser);

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
        when(userDatabase.findUser("existinguser")).thenReturn(mockUser);

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
        session.setAttribute(Constants.USER_KEY, mockUser);

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
        session.setAttribute(Constants.USER_KEY, mockUser);

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
    }

    @Test
    void saveRegistration_EditMode_WithoutPassword_ShouldKeepOldPassword() throws Exception {
        session.setAttribute(Constants.USER_KEY, mockUser);

        mockMvc.perform(post("/saveRegistration")
                .param("action", "Edit")
                .param("username", "testuser")
                .param("fullName", "Test User")
                .param("fromAddress", "test@example.com")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"));

        verify(userDatabase).save();
    }
}
