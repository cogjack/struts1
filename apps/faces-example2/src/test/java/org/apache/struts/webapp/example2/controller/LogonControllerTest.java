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
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogonController.class)
class LogonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDatabase userDatabase;

    @Test
    void showLogonForm_ShouldReturnLogonView() throws Exception {
        mockMvc.perform(get("/editLogon"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeExists("logonForm"));
    }

    @Test
    void processLogon_WithValidCredentials_ShouldRedirectToMainMenu() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn("password");
        when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/mainMenu"))
            .andExpect(request().sessionAttribute(Constants.USER_KEY, user));
    }

    @Test
    void processLogon_WithInvalidUsername_ShouldReturnLogonWithError() throws Exception {
        when(userDatabase.findUser("nonexistent")).thenReturn(null);

        mockMvc.perform(post("/logon")
                .param("username", "nonexistent")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_WithInvalidPassword_ShouldReturnLogonWithError() throws Exception {
        User user = mock(User.class);
        when(user.getPassword()).thenReturn("correctpassword");
        when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "wrongpassword"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_WithBlankUsername_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void processLogon_WithBlankPassword_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void processLogon_WithUsernameTooShort_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "ab")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void processLogon_WithUsernameTooLong_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "thisusernameiswaytoolong")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "username"));
    }

    @Test
    void processLogon_WithPasswordTooShort_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "ab"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void processLogon_WithPasswordTooLong_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "thispasswordiswaytoolong"))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void processLogon_WithBothFieldsBlank_ShouldFailValidation() throws Exception {
        mockMvc.perform(post("/logon")
                .param("username", "")
                .param("password", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("logon"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "username"))
            .andExpect(model().attributeHasFieldErrors("logonForm", "password"));
    }

    @Test
    void processLogon_WithValidCredentials_ShouldStoreUserInSession() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");
        when(user.getPassword()).thenReturn("validpass");
        when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                .param("username", "testuser")
                .param("password", "validpass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(request().sessionAttribute(Constants.USER_KEY, user));
    }
}
