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
import org.apache.struts.webapp.example2.form.RegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring MVC controller handling user registration (create and edit).
 * Replaces the Struts EditRegistrationAction and SaveRegistrationAction.
 */
@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final UserDatabase userDatabase;

    public RegistrationController(UserDatabase userDatabase) {
        this.userDatabase = userDatabase;
    }

    @GetMapping("/editRegistration")
    public String editRegistration(
            @RequestParam(value = "action", defaultValue = "Create") String action,
            HttpSession session,
            Model model) {

        if (log.isDebugEnabled()) {
            log.debug("RegistrationController: Processing {} action", action);
        }

        User user = null;
        if (!"Create".equals(action)) {
            user = (User) session.getAttribute(Constants.USER_KEY);
            if (user == null) {
                if (log.isDebugEnabled()) {
                    log.debug("User is not logged on in session {}", session.getId());
                }
                return "redirect:/editLogon";
            }
        }

        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setAction(action);

        if (user != null) {
            if (log.isTraceEnabled()) {
                log.trace("Populating form from {}", user);
            }
            registrationForm.setUsername(user.getUsername());
            registrationForm.setFullName(user.getFullName());
            registrationForm.setFromAddress(user.getFromAddress());
            registrationForm.setReplyToAddress(user.getReplyToAddress());
        }

        model.addAttribute("registrationForm", registrationForm);

        if (user != null) {
            model.addAttribute("user", user);
        }

        if (log.isTraceEnabled()) {
            log.trace("Forwarding to registration page");
        }

        return "registration";
    }

    @PostMapping("/saveRegistration")
    public String saveRegistration(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult result,
            HttpSession session,
            Model model) {

        String action = registrationForm.getAction();
        if (action == null) {
            action = "Create";
        }

        if (log.isDebugEnabled()) {
            log.debug("RegistrationController: Processing save for {} action", action);
        }

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (!"Create".equals(action) && user == null) {
            if (log.isTraceEnabled()) {
                log.trace("User is not logged on in session {}", session.getId());
            }
            return "redirect:/editLogon";
        }

        if ("Create".equals(action)) {
            String password = registrationForm.getPassword();
            if (password == null || password.isEmpty()) {
                result.rejectValue("password", "error.password.required");
            }
            String password2 = registrationForm.getPassword2();
            if (password2 == null || password2.isEmpty()) {
                result.rejectValue("password2", "error.password2.required");
            }

            if (userDatabase.findUser(registrationForm.getUsername()) != null) {
                result.rejectValue("username", "error.username.unique",
                        new Object[]{registrationForm.getUsername()}, null);
            }
        }

        if (result.hasErrors()) {
            if (log.isDebugEnabled()) {
                log.debug("Validation errors found, returning to registration form");
            }
            if (user != null) {
                model.addAttribute("user", user);
            }
            return "registration";
        }

        try {
            if ("Create".equals(action)) {
                user = userDatabase.createUser(registrationForm.getUsername());
            }

            String oldPassword = user.getPassword();

            user.setFullName(registrationForm.getFullName());
            user.setFromAddress(registrationForm.getFromAddress());
            user.setReplyToAddress(registrationForm.getReplyToAddress());

            String newPassword = registrationForm.getPassword();
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(newPassword);
            } else {
                user.setPassword(oldPassword);
            }

        } catch (Exception e) {
            log.error("Registration.populate", e);
            result.reject("error.general", "An error occurred while saving registration");
            if (user != null) {
                model.addAttribute("user", user);
            }
            return "registration";
        }

        try {
            userDatabase.save();
        } catch (Exception e) {
            log.error("Database save", e);
        }

        if ("Create".equals(action)) {
            session.setAttribute(Constants.USER_KEY, user);
            if (log.isTraceEnabled()) {
                log.trace("User '{}' logged on in session {}", user.getUsername(), session.getId());
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Forwarding to success page");
        }

        return "redirect:/mainMenu";
    }
}
