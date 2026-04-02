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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogoffController.class)
class LogoffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoff_ShouldRedirectToWelcome() throws Exception {
        mockMvc.perform(get("/logoff"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    void logoff_WithUserInSession_ShouldInvalidateSession() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, "subscription");

        mockMvc.perform(get("/logoff").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoff_WithoutUserInSession_ShouldStillInvalidateSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/logoff").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoff_ShouldRemoveUserAttribute() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/logoff").session(session))
            .andExpect(status().is3xxRedirection());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoff_ShouldRemoveSubscriptionAttribute() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.SUBSCRIPTION_KEY, "subscription-data");

        mockMvc.perform(get("/logoff").session(session))
            .andExpect(status().is3xxRedirection());

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoff_WithBothAttributes_ShouldRemoveBothAndInvalidate() throws Exception {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, "subscription-data");

        mockMvc.perform(get("/logoff").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoff_ViaPost_ShouldNotBeAllowed() throws Exception {
        mockMvc.perform(post("/logoff"))
            .andExpect(status().isMethodNotAllowed());
    }

}
