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
import org.apache.struts.webapp.example2.config.DatabaseConfiguration.LabelValueBean;
import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.apache.struts.webapp.example2.form.SubscriptionForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final UserDatabase userDatabase;
    private final List<LabelValueBean> serverTypes;

    public SubscriptionController(UserDatabase userDatabase, List<LabelValueBean> serverTypes) {
        this.userDatabase = userDatabase;
        this.serverTypes = serverTypes;
    }

    @GetMapping("/editSubscription")
    public String showSubscriptionForm(@RequestParam(value = "action", defaultValue = "Create") String action,
                                       @RequestParam(value = "host", required = false) String host,
                                       HttpSession session,
                                       Model model) {
        log.debug("EditSubscription: Processing {} action", action);

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (user == null) {
            log.trace("User is not logged on in session {}", session.getId());
            return "redirect:/logon";
        }

        SubscriptionForm form = new SubscriptionForm();
        form.setAction(action);

        if (!"Create".equals(action)) {
            Subscription subscription = user.findSubscription(host);
            if (subscription == null) {
                log.trace("No subscription for user {} and host {}", user.getUsername(), host);
                return "redirect:/editRegistration";
            }
            session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);
            form.setHost(subscription.getHost());
            form.setAutoConnect(subscription.getAutoConnect());
            form.setPassword(subscription.getPassword());
            form.setType(subscription.getType());
            form.setUsername(subscription.getUsername());
        }

        model.addAttribute("subscriptionForm", form);
        model.addAttribute("serverTypes", serverTypes);
        return "subscription";
    }

    @PostMapping("/saveSubscription")
    public String saveSubscription(@Valid @ModelAttribute("subscriptionForm") SubscriptionForm form,
                                   BindingResult result,
                                   HttpSession session,
                                   Model model) {
        String action = form.getAction();
        if (action == null) {
            action = "?";
        }
        log.debug("SaveSubscription: Processing {} action", action);

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (user == null) {
            log.trace("User is not logged on in session {}", session.getId());
            return "redirect:/logon";
        }

        if ("Delete".equals(action)) {
            Subscription subscription = (Subscription) session.getAttribute(Constants.SUBSCRIPTION_KEY);
            if (subscription == null) {
                subscription = user.findSubscription(form.getHost());
            }
            if (subscription != null) {
                log.trace("Deleting mail server '{}' for user '{}'", subscription.getHost(), user.getUsername());
                user.removeSubscription(subscription);
                session.removeAttribute(Constants.SUBSCRIPTION_KEY);
                try {
                    userDatabase.save();
                } catch (Exception e) {
                    log.error("Database save", e);
                }
            }
            return "redirect:/editRegistration";
        }

        if (result.hasErrors()) {
            model.addAttribute("serverTypes", serverTypes);
            return "subscription";
        }

        Subscription subscription;
        if ("Create".equals(action)) {
            log.trace("Creating subscription for mail server '{}'", form.getHost());
            subscription = user.createSubscription(form.getHost());
        } else {
            subscription = (Subscription) session.getAttribute(Constants.SUBSCRIPTION_KEY);
            if (subscription == null) {
                subscription = user.findSubscription(form.getHost());
            }
            if (subscription == null) {
                log.trace("Missing subscription for user '{}'", user.getUsername());
                return "redirect:/editRegistration";
            }
        }

        subscription.setAutoConnect(form.getAutoConnect());
        subscription.setPassword(form.getPassword());
        subscription.setType(form.getType());
        subscription.setUsername(form.getUsername());

        try {
            userDatabase.save();
        } catch (Exception e) {
            log.error("Database save", e);
        }

        session.removeAttribute(Constants.SUBSCRIPTION_KEY);
        return "redirect:/editRegistration";
    }
}
