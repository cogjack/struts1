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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.memory.MemoryUser;
import org.apache.struts.webapp.example2.memory.MemoryUserDatabase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpSession;

public class LogoffControllerTest {

    private LogoffController controller;

    @Before
    public void setUp() {
        controller = new LogoffController();
    }

    @Test
    public void logoff_ShouldReturnRedirectToWelcome() {
        MockHttpSession session = new MockHttpSession();

        String result = controller.logoff(session);

        assertEquals("redirect:/welcome", result);
    }

    @Test
    public void logoff_WithUserInSession_ShouldInvalidateSession() {
        MockHttpSession session = new MockHttpSession();
        MemoryUserDatabase database = new MemoryUserDatabase();
        User user = new MemoryUser(database, "testuser");
        session.setAttribute(Constants.USER_KEY, user);
        session.setAttribute(Constants.SUBSCRIPTION_KEY, "subscription");

        controller.logoff(session);

        assertTrue("Session should be invalidated", session.isInvalid());
    }

    @Test
    public void logoff_WithoutUserInSession_ShouldStillInvalidateSession() {
        MockHttpSession session = new MockHttpSession();

        controller.logoff(session);

        assertTrue("Session should be invalidated", session.isInvalid());
    }

}
