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

import org.apache.struts.webapp.example2.domain.MemoryUserDatabase;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link LogonController} using MockMvc.
 */
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
        MemoryUserDatabase db = new MemoryUserDatabase();
        User user = db.createUser("testuser");
        user.setPassword("password");
        org.mockito.Mockito.when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                        .param("username", "testuser")
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));
    }

    @Test
    void processLogon_WithInvalidCredentials_ShouldReturnLogonWithError() throws Exception {
        org.mockito.Mockito.when(userDatabase.findUser("testuser")).thenReturn(null);

        mockMvc.perform(post("/logon")
                        .param("username", "testuser")
                        .param("password", "wrongpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("logon"))
                .andExpect(model().hasErrors());
    }

    @Test
    void processLogon_WithWrongPassword_ShouldReturnLogonWithError() throws Exception {
        MemoryUserDatabase db = new MemoryUserDatabase();
        User user = db.createUser("testuser");
        user.setPassword("correctpass");
        org.mockito.Mockito.when(userDatabase.findUser("testuser")).thenReturn(user);

        mockMvc.perform(post("/logon")
                        .param("username", "testuser")
                        .param("password", "wrongpass"))
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
}
