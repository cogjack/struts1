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

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoffController {

    private static final Logger log = LoggerFactory.getLogger(LogoffController.class);

    @GetMapping("/logoff")
    public String logoff(HttpSession session) {
        User user = (User) session.getAttribute(Constants.USER_KEY);

        if (user != null) {
            if (log.isDebugEnabled()) {
                log.debug("LogoffController: User '{}' logged off in session {}", 
                          user.getUsername(), session.getId());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("LogoffController: User logged off in session {}", 
                          session.getId());
            }
        }

        session.removeAttribute(Constants.SUBSCRIPTION_KEY);
        session.removeAttribute(Constants.USER_KEY);
        session.invalidate();

        return "redirect:/welcome";
    }
}
