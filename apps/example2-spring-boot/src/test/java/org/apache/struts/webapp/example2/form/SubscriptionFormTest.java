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

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private SubscriptionForm validForm() {
        SubscriptionForm form = new SubscriptionForm();
        form.setHost("mail.example.com");
        form.setUsername("testuser");
        form.setPassword("secret");
        form.setType("imap");
        form.setAutoConnect(false);
        form.setAction("Create");
        return form;
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        SubscriptionForm form = validForm();
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankHost_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setHost("");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("host");
    }

    @Test
    void nullHost_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setHost(null);
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("host");
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setUsername("");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    void nullUsername_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setUsername(null);
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("username");
    }

    @Test
    void imapType_ShouldBeValid() {
        SubscriptionForm form = validForm();
        form.setType("imap");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void pop3Type_ShouldBeValid() {
        SubscriptionForm form = validForm();
        form.setType("pop3");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void invalidType_ShouldHaveViolation() {
        SubscriptionForm form = validForm();
        form.setType("smtp");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("type");
    }

    @Test
    void nullType_ShouldBeValid() {
        SubscriptionForm form = validForm();
        form.setType(null);
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void defaultAction_ShouldBeCreate() {
        SubscriptionForm form = new SubscriptionForm();
        assertThat(form.getAction()).isEqualTo("Create");
    }

    @Test
    void defaultAutoConnect_ShouldBeFalse() {
        SubscriptionForm form = new SubscriptionForm();
        assertThat(form.getAutoConnect()).isFalse();
    }

    @Test
    void multipleViolations_BlankHostAndUsername() {
        SubscriptionForm form = validForm();
        form.setHost("");
        form.setUsername("");
        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);
        assertThat(violations).hasSize(2);
    }
}
