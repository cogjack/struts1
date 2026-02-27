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
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
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
 * Phase 5.3 - Full Request Integration Tests for Registration.
 * Tests the complete registration flow including user creation and editing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class RegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    // --- GET /editRegistration ---

    @Test
    void getCreateRegistration_ReturnsOkWithForm() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Create"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void getCreateRegistration_DefaultActionIsCreate() throws Exception {
        mockMvc.perform(get("/editRegistration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"));
    }

    @Test
    void getEditRegistration_WithoutSession_RedirectsToLogon() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    @Test
    void getEditRegistration_WithSession_ReturnsFormWithUserData() throws Exception {
        // First log in
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Then access registration edit
        mockMvc.perform(get("/editRegistration")
                        .param("action", "Edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"))
                .andExpect(model().attributeExists("user"));
    }

    // --- POST /saveRegistration (Create) ---

    @Test
    void postSaveRegistration_CreateNewUser_RedirectsToMainMenu() throws Exception {
        String uniqueUsername = "newuser_" + System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", uniqueUsername)
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("replyToAddress", "reply@example.com")
                        .param("password", "newpass")
                        .param("password2", "newpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"))
                .andReturn();

        // Verify user is stored in session (logged in)
        User sessionUser = (User) result.getRequest().getSession()
                .getAttribute(Constants.USER_KEY);
        assertThat(sessionUser).isNotNull();
        assertThat(sessionUser.getUsername()).isEqualTo(uniqueUsername);

        // Verify user was created in database
        User dbUser = userDatabase.findUser(uniqueUsername);
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getFullName()).isEqualTo("New User");
        assertThat(dbUser.getFromAddress()).isEqualTo("new@example.com");
        assertThat(dbUser.getPassword()).isEqualTo("newpass");
    }

    @Test
    void postSaveRegistration_DuplicateUsername_ReturnsError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "user")
                        .param("fullName", "Duplicate User")
                        .param("fromAddress", "dup@example.com")
                        .param("password", "pass123")
                        .param("password2", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
    }

    @Test
    void postSaveRegistration_MismatchedPasswords_ReturnsError() throws Exception {
        String uniqueUsername = "mismatch_" + System.currentTimeMillis();

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", uniqueUsername)
                        .param("fullName", "Mismatch User")
                        .param("fromAddress", "mismatch@example.com")
                        .param("password", "pass1")
                        .param("password2", "pass2"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors());
    }

    @Test
    void postSaveRegistration_BlankPassword_ReturnsError() throws Exception {
        String uniqueUsername = "blankpass_" + System.currentTimeMillis();

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", uniqueUsername)
                        .param("fullName", "Blank Pass User")
                        .param("fromAddress", "blankpass@example.com")
                        .param("password", "")
                        .param("password2", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors());
    }

    @Test
    void postSaveRegistration_MissingRequiredFields_ReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "")
                        .param("fullName", "")
                        .param("fromAddress", "")
                        .param("password", "pass")
                        .param("password2", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fullName"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));
    }

    @Test
    void postSaveRegistration_InvalidEmailFormat_ReturnsError() throws Exception {
        String uniqueUsername = "bademail_" + System.currentTimeMillis();

        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", uniqueUsername)
                        .param("fullName", "Bad Email User")
                        .param("fromAddress", "not-an-email")
                        .param("password", "pass123")
                        .param("password2", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));
    }

    // --- POST /saveRegistration (Edit) ---

    @Test
    void postSaveRegistration_EditExistingUser_UpdatesAndRedirects() throws Exception {
        // First log in
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Edit registration
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "user")
                        .param("fullName", "Updated Name")
                        .param("fromAddress", "updated@example.com")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));

        // Verify user was updated
        User updatedUser = userDatabase.findUser("user");
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getFromAddress()).isEqualTo("updated@example.com");
    }

    @Test
    void postSaveRegistration_EditWithoutSession_RedirectsToLogon() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "user")
                        .param("fullName", "Unauthorized Edit")
                        .param("fromAddress", "unauth@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }
}
