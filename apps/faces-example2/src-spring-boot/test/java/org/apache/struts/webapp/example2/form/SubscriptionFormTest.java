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
 * Unit tests for {@link SubscriptionForm} Bean Validation constraints.
 */
class SubscriptionFormTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SubscriptionForm validForm() {
        SubscriptionForm form = new SubscriptionForm();
        form.setHost("mail.example.com");
        form.setUsername("mailuser");
        form.setPassword("mailpass");
        form.setType("imap");
        form.setAutoConnect(false);
        return form;
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(validForm());
        assertThat(violations).isEmpty();
    }

    @Test
    void validFormWithPop3_ShouldHaveNoViolations() {
        SubscriptionForm form = validForm();
        form.setType("pop3");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankHost_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setHost("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("host"));
    }

    @Test
    void nullHost_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setHost(null);

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("host"));
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setUsername("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void nullUsername_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setUsername(null);

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankPassword_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setPassword("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void nullPassword_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setPassword(null);

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void blankType_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setType("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    void nullType_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setType(null);

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    void invalidType_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setType("smtp");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(
                v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    void defaultActionIsCreate() {
        SubscriptionForm form = new SubscriptionForm();
        assertThat(form.getAction()).isEqualTo("Create");
    }

    @Test
    void autoConnectDefaultIsFalse() {
        SubscriptionForm form = new SubscriptionForm();
        assertThat(form.isAutoConnect()).isFalse();
    }

    @Test
    void setAndGetAllFields() {
        SubscriptionForm form = new SubscriptionForm();
        form.setAction("Edit");
        form.setHost("mail.test.com");
        form.setUsername("user1");
        form.setPassword("pass1");
        form.setType("pop3");
        form.setAutoConnect(true);

        assertThat(form.getAction()).isEqualTo("Edit");
        assertThat(form.getHost()).isEqualTo("mail.test.com");
        assertThat(form.getUsername()).isEqualTo("user1");
        assertThat(form.getPassword()).isEqualTo("pass1");
        assertThat(form.getType()).isEqualTo("pop3");
        assertThat(form.isAutoConnect()).isTrue();
    }
}
