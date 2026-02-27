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
 * Phase 5.4 - End-to-End User Journey Tests.
 * Tests complete user journeys through the application simulating real user behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class EndToEndUserJourneyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    /**
     * Complete user journey:
     * Register → Login → Add Subscription → Edit Subscription → Delete Subscription → Logout
     */
    @Test
    void completeUserJourney() throws Exception {
        String username = "journey_" + System.currentTimeMillis();
        String subscriptionHost = "mail.journey" + System.currentTimeMillis() + ".com";

        // Step 1: Access welcome page
        mockMvc.perform(get("/welcome"))
                .andExpect(status().isOk());

        // Step 2: Register a new user
        MvcResult registerResult = mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", username)
                        .param("fullName", "Journey User")
                        .param("fromAddress", "journey@example.com")
                        .param("replyToAddress", "reply@example.com")
                        .param("password", "journeypass")
                        .param("password2", "journeypass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) registerResult.getRequest().getSession();

        // Verify user is now logged in after registration
        assertThat(session.getAttribute(Constants.USER_KEY)).isNotNull();

        // Step 3: Access main menu
        mockMvc.perform(get("/mainMenu").session(session))
                .andExpect(status().isOk());

        // Step 4: Navigate to registration page to view subscriptions
        mockMvc.perform(get("/editRegistration")
                        .param("action", "Edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"));

        // Step 5: Add a new subscription
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Create")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"));

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", subscriptionHost)
                        .param("username", "journeymail")
                        .param("password", "mailpass")
                        .param("type", "imap")
                        .param("autoConnect", "true")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was created
        User user = userDatabase.findUser(username);
        Subscription createdSub = user.findSubscription(subscriptionHost);
        assertThat(createdSub).isNotNull();
        assertThat(createdSub.getUsername()).isEqualTo("journeymail");
        assertThat(createdSub.getType()).isEqualTo("imap");
        assertThat(createdSub.getAutoConnect()).isTrue();

        // Step 6: Edit the subscription
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Edit")
                        .param("host", subscriptionHost)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"));

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Edit")
                        .param("host", subscriptionHost)
                        .param("username", "updatedmail")
                        .param("password", "updatedpass")
                        .param("type", "pop3")
                        .param("autoConnect", "false")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was updated
        Subscription updatedSub = user.findSubscription(subscriptionHost);
        assertThat(updatedSub.getUsername()).isEqualTo("updatedmail");
        assertThat(updatedSub.getType()).isEqualTo("pop3");

        // Step 7: Delete the subscription
        mockMvc.perform(get("/editSubscription")
                        .param("action", "Delete")
                        .param("host", subscriptionHost)
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Delete")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editRegistration"));

        // Verify subscription was removed
        assertThat(user.findSubscription(subscriptionHost)).isNull();

        // Step 8: Logout
        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        // Session should be invalidated
        assertThat(session.isInvalid()).isTrue();
    }

    /**
     * Invalid login scenario: wrong credentials show error message.
     */
    @Test
    void invalidLoginScenario_WrongCredentials_ShowsError() throws Exception {
        // Access logon page
        mockMvc.perform(get("/editLogon"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"));

        // Submit invalid credentials
        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    /**
     * Invalid login scenario: non-existent user shows error message.
     */
    @Test
    void invalidLoginScenario_NonExistentUser_ShowsError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "doesnotexist")
                        .param("password", "somepassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    /**
     * Validation error scenario: submit registration form with invalid data.
     */
    @Test
    void validationErrorScenario_InvalidRegistration_ShowsFieldErrors() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "")
                        .param("fullName", "")
                        .param("fromAddress", "invalid-email")
                        .param("password", "pass1")
                        .param("password2", "pass2"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fullName"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));
    }

    /**
     * Validation error scenario: submit subscription form with blank fields.
     */
    @Test
    void validationErrorScenario_InvalidSubscription_ShowsFieldErrors() throws Exception {
        // First log in
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Submit subscription with empty fields
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "")
                        .param("username", "")
                        .param("password", "")
                        .param("type", "")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("subscription"))
                .andExpect(model().hasErrors());
    }

    /**
     * Session protection: unauthenticated users cannot access protected resources.
     */
    @Test
    void sessionProtection_UnauthenticatedAccess_RedirectsToLogon() throws Exception {
        // Try to edit registration without logging in
        mockMvc.perform(get("/editRegistration").param("action", "Edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));

        // Try to create subscription without logging in
        mockMvc.perform(get("/editSubscription").param("action", "Create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));

        // Try to save subscription without logging in
        mockMvc.perform(post("/saveSubscription")
                        .param("action", "Create")
                        .param("host", "mail.test.com")
                        .param("username", "test")
                        .param("password", "test")
                        .param("type", "imap"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/editLogon"));
    }

    /**
     * Login → Register edit → Logout → Verify session gone.
     */
    @Test
    void loginEditLogoutJourney() throws Exception {
        // Log in
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Edit registration
        mockMvc.perform(get("/editRegistration")
                        .param("action", "Edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"));

        // Log off
        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }
}
