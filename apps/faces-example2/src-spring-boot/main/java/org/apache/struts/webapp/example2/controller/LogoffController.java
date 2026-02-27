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

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spring MVC controller that handles user logoff.
 * Migrated from the Struts {@code LogoffAction}.
 *
 * <p>Invalidates the current HTTP session and redirects to the welcome page.</p>
 */
@Controller
public class LogoffController {

    private static final Logger log = LoggerFactory.getLogger(LogoffController.class);

    /**
     * Process a user logoff request by invalidating the current session
     * and redirecting to the welcome page.
     *
     * @param session the current HTTP session
     * @return a redirect to the welcome page
     */
    @GetMapping("/logoff")
    public String logoff(HttpSession session) {
        if (log.isDebugEnabled()) {
            log.debug("User logged off in session {}", session.getId());
        }
        session.invalidate();
        return "redirect:/welcome";
    }
}
