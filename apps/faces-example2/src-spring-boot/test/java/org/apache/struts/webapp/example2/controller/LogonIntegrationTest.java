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
 * Phase 5.3 - Full Request Integration Tests for Logon.
 * Tests complete HTTP request/response cycles through the Spring MVC stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class LogonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void getLogonPage_ReturnsOkWithForm() throws Exception {
        mockMvc.perform(get("/editLogon"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeExists("logonForm"));
    }

    @Test
    void getLogonPage_ContainsLogonContent() throws Exception {
        mockMvc.perform(get("/editLogon"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("username")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("password")));
    }

    @Test
    void postLogon_ValidCredentials_RedirectsToMainMenu() throws Exception {
        MvcResult result = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"))
                .andReturn();

        // Verify user is stored in session
        User sessionUser = (User) result.getRequest().getSession()
                .getAttribute(Constants.USER_KEY);
        assertThat(sessionUser).isNotNull();
        assertThat(sessionUser.getUsername()).isEqualTo("user");
    }

    @Test
    void postLogon_InvalidUsername_ReturnsLogonWithError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "nonexistent")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    @Test
    void postLogon_WrongPassword_ReturnsLogonWithError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    @Test
    void postLogon_BlankUsername_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void postLogon_BlankPassword_ReturnsValidationError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void postLogon_BothBlank_ReturnsValidationErrors() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "username", "password"));
    }

    @Test
    void logoff_InvalidatesSessionAndRedirects() throws Exception {
        // First log in
        MvcResult loginResult = mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Then log off
        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        // Session should be invalidated
        assertThat(session.isInvalid()).isTrue();
    }
}
