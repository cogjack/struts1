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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link LogoffController}.
 * Uses {@link WebMvcTest} to test the controller in isolation with MockMvc.
 */
@WebMvcTest(LogoffController.class)
class LogoffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoffInvalidatesSessionAndRedirectsToWelcome() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("user", "testUser");

        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoffWithNoSessionAttributesStillRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/logoff").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logoffReturnsRedirectStatus302() throws Exception {
        mockMvc.perform(get("/logoff"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }
}
