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
 * Unit tests for {@link RegistrationForm} Bean Validation constraints.
 */
class RegistrationFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegistrationForm validForm() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setPassword("password");
        form.setPassword2("password");
        return form;
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(validForm());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setUsername("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void nullUsername_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setUsername(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankFullName_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setFullName("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void nullFullName_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setFullName(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void blankFromAddress_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setFromAddress("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void nullFromAddress_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setFromAddress(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidFromAddress_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setFromAddress("not-an-email");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidReplyToAddress_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setReplyToAddress("not-an-email");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("replyToAddress"));
    }

    @Test
    void validReplyToAddress_ShouldHaveNoViolation() {
        RegistrationForm form = validForm();
        form.setReplyToAddress("reply@example.com");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void nullReplyToAddress_ShouldHaveNoViolation() {
        RegistrationForm form = validForm();
        form.setReplyToAddress(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void passwordMismatch_ShouldHaveViolation() {
        RegistrationForm form = validForm();
        form.setPassword("password1");
        form.setPassword2("password2");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void passwordsMatch_ShouldHaveNoViolation() {
        RegistrationForm form = validForm();
        form.setPassword("samepass");
        form.setPassword2("samepass");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void bothPasswordsNull_ShouldHaveNoViolation() {
        RegistrationForm form = validForm();
        form.setPassword(null);
        form.setPassword2(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void defaultActionIsCreate() {
        RegistrationForm form = new RegistrationForm();
        assertThat(form.getAction()).isEqualTo("Create");
    }

    @Test
    void setAndGetAllFields() {
        RegistrationForm form = new RegistrationForm();
        form.setAction("Edit");
        form.setUsername("user1");
        form.setFullName("Full Name");
        form.setFromAddress("from@test.com");
        form.setReplyToAddress("reply@test.com");
        form.setPassword("pass");
        form.setPassword2("pass");

        assertThat(form.getAction()).isEqualTo("Edit");
        assertThat(form.getUsername()).isEqualTo("user1");
        assertThat(form.getFullName()).isEqualTo("Full Name");
        assertThat(form.getFromAddress()).isEqualTo("from@test.com");
        assertThat(form.getReplyToAddress()).isEqualTo("reply@test.com");
        assertThat(form.getPassword()).isEqualTo("pass");
        assertThat(form.getPassword2()).isEqualTo("pass");
    }
}
