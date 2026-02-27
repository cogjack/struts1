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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LogonForm} Bean Validation constraints.
 */
class LogonFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword("password");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("");
        form.setPassword("password");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankPassword_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword("");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void usernameTooShort_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("ab");
        form.setPassword("password");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void usernameTooLong_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("a".repeat(17));
        form.setPassword("password");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void passwordTooShort_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword("ab");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void passwordTooLong_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword("a".repeat(17));

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void nullUsername_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername(null);
        form.setPassword("password");

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void nullPassword_ShouldHaveViolation() {
        LogonForm form = new LogonForm();
        form.setUsername("testuser");
        form.setPassword(null);

        Set<ConstraintViolation<LogonForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }
}
