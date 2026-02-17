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
import org.apache.struts.webapp.example2.exception.ExpiredPasswordException;
import org.apache.struts.webapp.example2.form.LogonForm;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogonController {

    private static final Logger log = LoggerFactory.getLogger(LogonController.class);

    private final UserDatabase userDatabase;

    public LogonController(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    @GetMapping("/editLogon")
    public String showLogonForm(Model model) {
        model.addAttribute("logonForm", new LogonForm());
        return "logon";
    }

    @PostMapping("/logon")
    public String processLogon(@Valid @ModelAttribute("logonForm") LogonForm form,
                               BindingResult result,
                               HttpSession session) {
        if (result.hasErrors()) {
            return "logon";
        }

        User user = getUser(form.getUsername());
        if (user != null && !user.getPassword().equals(form.getPassword())) {
            user = null;
        }
        if (user == null) {
            result.reject("error.password.mismatch");
            return "logon";
        }

        session.setAttribute(Constants.USER_KEY, user);
        log.debug("User '{}' logged on in session {}", user.getUsername(), session.getId());

        return "redirect:/mainMenu";
    }

    @GetMapping("/logoff")
    public String logoff(HttpSession session) {
        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (user != null) {
            log.debug("User '{}' logged off in session {}", user.getUsername(), session.getId());
        } else {
            log.debug("User logged off in session {}", session.getId());
        }
        session.removeAttribute(Constants.SUBSCRIPTION_KEY);
        session.removeAttribute(Constants.USER_KEY);
        session.invalidate();

        return "redirect:/welcome";
    }

    @ExceptionHandler(ExpiredPasswordException.class)
    public String handleExpiredPassword(ExpiredPasswordException ex, Model model) {
        model.addAttribute("username", ex.getUsername());
        return "changePassword";
    }

    private User getUser(String username) {
        if ("arithmetic".equals(username)) {
            throw new ArithmeticException();
        }
        if ("expired".equals(username)) {
            throw new ExpiredPasswordException(username);
        }
        return userDatabase.findUser(username);
    }
}
