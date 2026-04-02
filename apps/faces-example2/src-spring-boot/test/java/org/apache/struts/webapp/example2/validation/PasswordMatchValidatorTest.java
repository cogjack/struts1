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
package org.apache.struts.webapp.example2.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.apache.struts.webapp.example2.form.RegistrationForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PasswordMatchValidator}.
 */
class PasswordMatchValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegistrationForm formWithPasswords(String password, String password2) {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setPassword(password);
        form.setPassword2(password2);
        return form;
    }

    @Test
    void matchingPasswords_ShouldPass() {
        RegistrationForm form = formWithPasswords("secret", "secret");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void mismatchedPasswords_ShouldFail() {
        RegistrationForm form = formWithPasswords("secret", "different");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void bothNull_ShouldPass() {
        RegistrationForm form = formWithPasswords(null, null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void passwordNullPassword2NotNull_ShouldFail() {
        RegistrationForm form = formWithPasswords(null, "secret");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void passwordNotNullPassword2Null_ShouldFail() {
        RegistrationForm form = formWithPasswords("secret", null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void bothEmpty_ShouldPass() {
        RegistrationForm form = formWithPasswords("", "");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        // Empty strings match each other, so PasswordMatch passes
        assertThat(violations).noneMatch(
                v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void passwordEmptyPassword2NonEmpty_ShouldFail() {
        RegistrationForm form = formWithPasswords("", "secret");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password2"));
    }
}
