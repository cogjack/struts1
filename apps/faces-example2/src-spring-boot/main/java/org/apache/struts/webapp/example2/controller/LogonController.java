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
import jakarta.validation.Valid;
import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.apache.struts.webapp.example2.form.LogonForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Spring MVC controller handling user authentication.
 * Replaces the Struts LogonAction with standard Spring MVC patterns.
 */
@Controller
public class LogonController {

    private static final Logger log = LoggerFactory.getLogger(LogonController.class);

    private final UserDatabase userDatabase;

    public LogonController(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    /**
     * Display the logon form.
     */
    @GetMapping("/editLogon")
    public String showLogonForm(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "logon";
    }

    /**
     * Process the logon form submission.
     * Validates form fields via Bean Validation, then checks credentials
     * against the UserDatabase. On success, stores the user in the session
     * and redirects to the main menu.
     */
    @PostMapping("/logon")
    public String processLogon(@Valid @ModelAttribute LogonForm form,
                               BindingResult result,
                               HttpSession session) {

        if (result.hasErrors()) {
            return "logon";
        }

        User user = userDatabase.findUser(form.getUsername());

        if (user == null || !form.getPassword().equals(user.getPassword())) {
            result.reject("error.password.mismatch");
            return "logon";
        }

        if (log.isDebugEnabled()) {
            log.debug("User '{}' logged on in session {}", form.getUsername(), session.getId());
        }
        session.setAttribute(Constants.USER_KEY, user);
        return "redirect:/mainMenu";
    }
}
