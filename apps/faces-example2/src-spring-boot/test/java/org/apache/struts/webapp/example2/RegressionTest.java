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
package org.apache.struts.webapp.example2;

import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
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
 * Phase 5.5 - Regression Test Coverage.
 * Verifies all items in the regression test checklist from Section 5.5.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class RegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    // =========================================================================
    // Login Regression Tests
    // =========================================================================

    @Nested
    class LoginRegression {

        @Test
        void loginWithValidCredentials_Succeeds() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/mainMenu"));
        }

        @Test
        void loginWithInvalidUsername_ShowsError() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "nonexistent")
                            .param("password", "pass"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"))
                    .andExpect(model().hasErrors());
        }

        @Test
        void loginWithInvalidPassword_ShowsError() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "wrongpassword"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"))
                    .andExpect(model().hasErrors());
        }

        @Test
        void loginWithBlankUsername_ShowsFieldError() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "")
                            .param("password", "pass"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"))
                    .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
        }

        @Test
        void loginWithBlankPassword_ShowsFieldError() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"))
                    .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
        }

        @Test
        void loginWithBothBlank_ShowsFieldErrors() throws Exception {
            mockMvc.perform(post("/logon")
                            .param("username", "")
                            .param("password", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"))
                    .andExpect(model().attributeHasFieldErrors("logonForm", "username", "password"));
        }

        @Test
        void loginStoresUserInSession() throws Exception {
            MvcResult result = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();

            User sessionUser = (User) result.getRequest().getSession()
                    .getAttribute(Constants.USER_KEY);
            assertThat(sessionUser).isNotNull();
            assertThat(sessionUser.getUsername()).isEqualTo("user");
        }
    }

    // =========================================================================
    // Registration Regression Tests
    // =========================================================================

    @Nested
    class RegistrationRegression {

        @Test
        void registerNewUser_Succeeds() throws Exception {
            String username = "regtest_" + System.currentTimeMillis();

            mockMvc.perform(post("/saveRegistration")
                            .param("action", "Create")
                            .param("username", username)
                            .param("fullName", "Regression User")
                            .param("fromAddress", "regtest@example.com")
                            .param("password", "regpass")
                            .param("password2", "regpass"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/mainMenu"));

            User createdUser = userDatabase.findUser(username);
            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getFullName()).isEqualTo("Regression User");
        }

        @Test
        void registerExistingUsername_ShowsError() throws Exception {
            mockMvc.perform(post("/saveRegistration")
                            .param("action", "Create")
                            .param("username", "user")
                            .param("fullName", "Duplicate")
                            .param("fromAddress", "dup@example.com")
                            .param("password", "pass")
                            .param("password2", "pass"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("registration"))
                    .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
        }

        @Test
        void registerMismatchedPasswords_ShowsError() throws Exception {
            String username = "mismatch_reg_" + System.currentTimeMillis();

            mockMvc.perform(post("/saveRegistration")
                            .param("action", "Create")
                            .param("username", username)
                            .param("fullName", "Mismatch")
                            .param("fromAddress", "mismatch@example.com")
                            .param("password", "pass1")
                            .param("password2", "pass2"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("registration"))
                    .andExpect(model().hasErrors());
        }

        @Test
        void registerBlankRequiredFields_ShowsErrors() throws Exception {
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
        void registerNewUser_IsAutomaticallyLoggedIn() throws Exception {
            String username = "autologin_" + System.currentTimeMillis();

            MvcResult result = mockMvc.perform(post("/saveRegistration")
                            .param("action", "Create")
                            .param("username", username)
                            .param("fullName", "Auto Login")
                            .param("fromAddress", "auto@example.com")
                            .param("password", "pass")
                            .param("password2", "pass"))
                    .andReturn();

            User sessionUser = (User) result.getRequest().getSession()
                    .getAttribute(Constants.USER_KEY);
            assertThat(sessionUser).isNotNull();
            assertThat(sessionUser.getUsername()).isEqualTo(username);
        }

        @Test
        void editRegistration_UpdatesUserProfile() throws Exception {
            // Log in
            MvcResult loginResult = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

            // Save edited registration
            mockMvc.perform(post("/saveRegistration")
                            .param("action", "Edit")
                            .param("username", "user")
                            .param("fullName", "Regression Updated Name")
                            .param("fromAddress", "regression.updated@example.com")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/mainMenu"));

            User updatedUser = userDatabase.findUser("user");
            assertThat(updatedUser.getFullName()).isEqualTo("Regression Updated Name");
        }
    }

    // =========================================================================
    // Subscription CRUD Regression Tests
    // =========================================================================

    @Nested
    class SubscriptionRegression {

        private MockHttpSession login() throws Exception {
            MvcResult loginResult = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();
            return (MockHttpSession) loginResult.getRequest().getSession();
        }

        @Test
        void createSubscription_Succeeds() throws Exception {
            MockHttpSession session = login();
            String host = "mail.regcreate" + System.currentTimeMillis() + ".com";

            mockMvc.perform(post("/saveSubscription")
                            .param("action", "Create")
                            .param("host", host)
                            .param("username", "reguser")
                            .param("password", "regpass")
                            .param("type", "imap")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/editRegistration"));

            User user = userDatabase.findUser("user");
            assertThat(user.findSubscription(host)).isNotNull();
        }

        @Test
        void readSubscription_DisplaysCorrectData() throws Exception {
            MockHttpSession session = login();

            mockMvc.perform(get("/editSubscription")
                            .param("action", "Edit")
                            .param("host", "mail.yahoo.com")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("subscription"))
                    .andExpect(model().attributeExists("subscriptionForm"));
        }

        @Test
        void updateSubscription_ChangesArePersisted() throws Exception {
            MockHttpSession session = login();

            // Create a subscription to update
            String host = "mail.regupdate" + System.currentTimeMillis() + ".com";
            User user = userDatabase.findUser("user");
            Subscription sub = user.createSubscription(host);
            sub.setType("imap");
            sub.setUsername("original");
            sub.setPassword("original");

            // Navigate to edit
            mockMvc.perform(get("/editSubscription")
                            .param("action", "Edit")
                            .param("host", host)
                            .session(session))
                    .andExpect(status().isOk());

            // Save updates
            mockMvc.perform(post("/saveSubscription")
                            .param("action", "Edit")
                            .param("host", host)
                            .param("username", "updated")
                            .param("password", "updated")
                            .param("type", "pop3")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/editRegistration"));

            Subscription updated = user.findSubscription(host);
            assertThat(updated.getUsername()).isEqualTo("updated");
            assertThat(updated.getType()).isEqualTo("pop3");
        }

        @Test
        void deleteSubscription_RemovesFromDatabase() throws Exception {
            MockHttpSession session = login();

            // Create a subscription to delete
            String host = "mail.regdelete" + System.currentTimeMillis() + ".com";
            User user = userDatabase.findUser("user");
            Subscription sub = user.createSubscription(host);
            sub.setType("imap");
            sub.setUsername("deluser");
            sub.setPassword("delpass");

            // Navigate to delete
            mockMvc.perform(get("/editSubscription")
                            .param("action", "Delete")
                            .param("host", host)
                            .session(session))
                    .andExpect(status().isOk());

            // Confirm delete
            mockMvc.perform(post("/saveSubscription")
                            .param("action", "Delete")
                            .session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/editRegistration"));

            assertThat(user.findSubscription(host)).isNull();
        }

        @Test
        void createSubscriptionWithDuplicateHost_ShowsError() throws Exception {
            MockHttpSession session = login();

            mockMvc.perform(post("/saveSubscription")
                            .param("action", "Create")
                            .param("host", "mail.yahoo.com")
                            .param("username", "dup")
                            .param("password", "dup")
                            .param("type", "imap")
                            .session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("subscription"))
                    .andExpect(model().attributeHasFieldErrors("subscriptionForm", "host"));
        }

        @Test
        void subscriptionRequiresAuthentication() throws Exception {
            mockMvc.perform(get("/editSubscription").param("action", "Create"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/editLogon"));
        }
    }

    // =========================================================================
    // Logout and Session Invalidation Regression Tests
    // =========================================================================

    @Nested
    class LogoutRegression {

        @Test
        void logoff_InvalidatesSession() throws Exception {
            MvcResult loginResult = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

            mockMvc.perform(get("/logoff").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/welcome"));

            assertThat(session.isInvalid()).isTrue();
        }

        @Test
        void logoff_RedirectsToWelcomePage() throws Exception {
            MvcResult loginResult = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

            mockMvc.perform(get("/logoff").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/welcome"));
        }
    }

    // =========================================================================
    // Welcome and Static Page Regression Tests
    // =========================================================================

    @Nested
    class PageRegression {

        @Test
        void welcomePageIsAccessible() throws Exception {
            mockMvc.perform(get("/welcome"))
                    .andExpect(status().isOk());
        }

        @Test
        void rootPageIsAccessible() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk());
        }

        @Test
        void mainMenuPageIsAccessible() throws Exception {
            // Main menu requires a logged-in user for session.user.username
            MvcResult loginResult = mockMvc.perform(post("/logon")
                            .param("username", "user")
                            .param("password", "pass"))
                    .andReturn();
            MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

            mockMvc.perform(get("/mainMenu").session(session))
                    .andExpect(status().isOk());
        }

        @Test
        void logonPageIsAccessible() throws Exception {
            mockMvc.perform(get("/editLogon"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("logon"));
        }

        @Test
        void registrationCreatePageIsAccessible() throws Exception {
            mockMvc.perform(get("/editRegistration").param("action", "Create"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("registration"));
        }
    }
}
