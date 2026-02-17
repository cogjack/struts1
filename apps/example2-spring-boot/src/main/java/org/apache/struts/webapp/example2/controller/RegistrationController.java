package org.apache.struts.webapp.example2.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.apache.struts.webapp.example2.form.RegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired
    private UserDatabase userDatabase;

    @GetMapping("/editRegistration")
    public String showRegistrationForm(
            @RequestParam(value = "action", defaultValue = "Create") String action,
            HttpSession session,
            Model model) {

        log.debug("EditRegistration: Processing {} action", action);

        RegistrationForm form = new RegistrationForm();

        if (!"Create".equals(action)) {
            User user = (User) session.getAttribute(Constants.USER_KEY);
            if (user == null) {
                log.debug("User is not logged on in session {}", session.getId());
                return "redirect:/logon";
            }
            form.setAction(action);
            form.setFromAddress(user.getFromAddress());
            form.setFullName(user.getFullName());
            form.setReplyToAddress(user.getReplyToAddress());
            form.setUsername(user.getUsername());
            form.setPassword(null);
            form.setPassword2(null);
        }

        model.addAttribute("registrationForm", form);
        return "registration";
    }

    @PostMapping("/saveRegistration")
    public String saveRegistration(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult result,
            HttpSession session,
            Model model) {

        String action = form.getAction();
        if (action == null) {
            action = "Create";
        }
        log.debug("SaveRegistration: Processing {} action", action);

        User user = (User) session.getAttribute(Constants.USER_KEY);
        if (!"Create".equals(action) && user == null) {
            log.debug("User is not logged on in session {}", session.getId());
            return "redirect:/logon";
        }

        if ("Create".equals(action)) {
            if (form.getPassword() == null || form.getPassword().isEmpty()) {
                result.rejectValue("password", "error.password.required");
            }
            if (form.getPassword2() == null || form.getPassword2().isEmpty()) {
                result.rejectValue("password2", "error.password2.required");
            }
            if (userDatabase.findUser(form.getUsername()) != null) {
                result.rejectValue("username", "error.username.unique",
                        new Object[]{form.getUsername()}, null);
            }
        }

        if (result.hasErrors()) {
            return "registration";
        }

        try {
            if ("Create".equals(action)) {
                user = userDatabase.createUser(form.getUsername());
            }
            String oldPassword = user.getPassword();
            user.setFromAddress(form.getFromAddress());
            user.setFullName(form.getFullName());
            user.setReplyToAddress(form.getReplyToAddress());
            if (form.getPassword() != null && !form.getPassword().isEmpty()) {
                user.setPassword(form.getPassword());
            } else {
                user.setPassword(oldPassword);
            }
        } catch (Exception e) {
            log.error("Registration.populate", e);
            throw e;
        }

        try {
            userDatabase.save();
        } catch (Exception e) {
            log.error("Database save", e);
        }

        if ("Create".equals(action)) {
            session.setAttribute(Constants.USER_KEY, user);
            log.debug("User '{}' logged on in session {}", user.getUsername(), session.getId());
        }

        return "redirect:/mainMenu";
    }
}
