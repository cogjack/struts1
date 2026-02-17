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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class LogonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void showLogonForm_returnsLogonView() throws Exception {
        mockMvc.perform(get("/editLogon"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeExists("logonForm"));
    }

    @Test
    void processLogon_validCredentials_redirectsToMainMenu() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "pass")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));

        Object userAttr = session.getAttribute(Constants.USER_KEY);
        assertTrue(userAttr instanceof User);
        User user = (User) userAttr;
        assertTrue("user".equals(user.getUsername()));
    }

    @Test
    void processLogon_invalidUsername_returnsLogonWithError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "nonexistent")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_invalidPassword_returnsLogonWithError() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", "wrongpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_blankUsername_returnsValidationErrors() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void processLogon_blankPassword_returnsValidationErrors() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "user")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void processLogon_blankUsernameAndPassword_returnsValidationErrors() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "username", "password"));
    }

    @Test
    void processLogon_usernameTooShort_returnsValidationErrors() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "ab")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void processLogon_expiredPassword_returnsChangePasswordView() throws Exception {
        mockMvc.perform(post("/logon")
                        .param("username", "expired")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("changePassword"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    void logoff_invalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        User user = userDatabase.findUser("user");
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, "someSub");

        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        assertTrue(session.isInvalid());
    }

    @Test
    void logoff_withNoUser_invalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        assertTrue(session.isInvalid());
    }
}
