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
package org.apache.struts.webapp.example2.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogonFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validForm_noViolations() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullUsername_hasViolation() {
        LogonForm form = new LogonForm();
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void blankUsername_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("");
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void blankPassword_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void nullPassword_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("user");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void usernameTooShort_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("ab");
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void usernameTooLong_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("a".repeat(17));
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void passwordTooShort_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("ab");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordTooLong_hasViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("a".repeat(17));

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void usernameAtMinLength_isValid() {
        LogonForm form = new LogonForm();
        form.setUsername("abc");
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void usernameAtMaxLength_isValid() {
        LogonForm form = new LogonForm();
        form.setUsername("a".repeat(16));
        form.setPassword("pass");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordAtMinLength_isValid() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("abc");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordAtMaxLength_isValid() {
        LogonForm form = new LogonForm();
        form.setUsername("user");
        form.setPassword("a".repeat(16));

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword("testpass");

        assertEquals("testuser", form.getUsername());
        assertEquals("testpass", form.getPassword());
    }
}
