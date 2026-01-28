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

import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.Subscription;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.apache.struts.webapp.example2.config.DatabaseConfiguration.LabelValueBean;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SubscriptionController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    private final UserDatabase userDatabase;
    private final List<LabelValueBean> serverTypes;

    public SubscriptionController(UserDatabase userDatabase, List<LabelValueBean> serverTypes) {
        this.userDatabase = userDatabase;
        this.serverTypes = serverTypes;
    }

    @ModelAttribute("serverTypes")
    public List<LabelValueBean> getServerTypes() {
        return serverTypes;
    }

    @GetMapping("/editSubscription")
    public String editSubscription(
            @RequestParam(value = "action", defaultValue = "Create") String action,
            @RequestParam(value = "host", required = false) String host,
            HttpSession session,
            Model model) {

        if (log.isDebugEnabled()) {
            log.debug("SubscriptionController: Processing {} action", action);
        }

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (user == null) {
            if (log.isTraceEnabled()) {
                log.trace("User is not logged on in session {}", session.getId());
            }
            return "redirect:/editLogon";
        }

        SubscriptionForm subscriptionForm = new SubscriptionForm();
        subscriptionForm.setAction(action);

        if (!"Create".equals(action)) {
            Subscription subscription = user.findSubscription(host);
            if (subscription == null) {
                if (log.isTraceEnabled()) {
                    log.trace("No subscription for user {} and host {}", user.getUsername(), host);
                }
                return "redirect:/editRegistration";
            }

            session.setAttribute(Constants.SUBSCRIPTION_KEY, subscription);

            subscriptionForm.setHost(subscription.getHost());
            subscriptionForm.setUsername(subscription.getUsername());
            subscriptionForm.setPassword(subscription.getPassword());
            subscriptionForm.setType(subscription.getType());
            subscriptionForm.setAutoConnect(subscription.getAutoConnect());

            if (log.isTraceEnabled()) {
                log.trace("Populated form from subscription: {}", subscription.getHost());
            }
        }

        model.addAttribute("subscriptionForm", subscriptionForm);
        model.addAttribute("user", user);

        if (log.isTraceEnabled()) {
            log.trace("Forwarding to subscription page");
        }

        return "subscription";
    }

    @PostMapping("/saveSubscription")
    public String saveSubscription(
            @Valid @ModelAttribute("subscriptionForm") SubscriptionForm subscriptionForm,
            BindingResult result,
            @RequestParam(value = "cancel", required = false) String cancel,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        String action = subscriptionForm.getAction();
        if (action == null) {
            action = "Create";
        }

        if (log.isDebugEnabled()) {
            log.debug("SubscriptionController: Processing save for {} action", action);
        }

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (user == null) {
            if (log.isTraceEnabled()) {
                log.trace("User is not logged on in session {}", session.getId());
            }
            return "redirect:/editLogon";
        }

        if (cancel != null) {
            if (log.isTraceEnabled()) {
                log.trace("Transaction '{}' was cancelled", action);
            }
            session.removeAttribute(Constants.SUBSCRIPTION_KEY);
            return "redirect:/editRegistration";
        }

        if ("Delete".equals(action)) {
            return handleDelete(user, session);
        }

        if (result.hasErrors()) {
            if (log.isDebugEnabled()) {
                log.debug("Validation errors found, returning to subscription form");
            }
            model.addAttribute("user", user);
            return "subscription";
        }

        Subscription subscription;
        if ("Create".equals(action)) {
            if (log.isTraceEnabled()) {
                log.trace("Creating subscription for mail server '{}'", subscriptionForm.getHost());
            }
            try {
                subscription = user.createSubscription(subscriptionForm.getHost());
            } catch (IllegalArgumentException e) {
                result.rejectValue("host", "error.host.unique",
                        new Object[]{subscriptionForm.getHost()}, null);
                model.addAttribute("user", user);
                return "subscription";
            }
        } else {
            subscription = (Subscription) session.getAttribute(Constants.SUBSCRIPTION_KEY);
            if (subscription == null) {
                if (log.isTraceEnabled()) {
                    log.trace("Missing subscription for user '{}'", user.getUsername());
                }
                return "redirect:/editRegistration";
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Populating subscription from form bean");
        }
        subscription.setUsername(subscriptionForm.getUsername());
        subscription.setPassword(subscriptionForm.getPassword());
        subscription.setType(subscriptionForm.getType());
        subscription.setAutoConnect(subscriptionForm.isAutoConnect());

        try {
            userDatabase.save();
        } catch (Exception e) {
            log.error("Database save error", e);
        }

        session.removeAttribute(Constants.SUBSCRIPTION_KEY);

        if (log.isTraceEnabled()) {
            log.trace("Forwarding to success page");
        }

        return "redirect:/editRegistration";
    }

    private String handleDelete(User user, HttpSession session) {
        Subscription subscription = (Subscription) session.getAttribute(Constants.SUBSCRIPTION_KEY);
        if (subscription == null) {
            if (log.isTraceEnabled()) {
                log.trace("Missing subscription for user '{}' during delete", user.getUsername());
            }
            return "redirect:/editRegistration";
        }

        if (log.isTraceEnabled()) {
            log.trace("Deleting mail server '{}' for user '{}'", subscription.getHost(), user.getUsername());
        }
        user.removeSubscription(subscription);
        session.removeAttribute(Constants.SUBSCRIPTION_KEY);

        try {
            userDatabase.save();
        } catch (Exception e) {
            log.error("Database save error", e);
        }

        return "redirect:/editRegistration";
    }
}
