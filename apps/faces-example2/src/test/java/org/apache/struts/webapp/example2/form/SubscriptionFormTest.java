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

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SubscriptionForm createValidForm() {
        SubscriptionForm form = new SubscriptionForm();
        form.setHost("mail.example.com");
        form.setUsername("mailuser");
        form.setPassword("mailpass");
        form.setType("imap");
        return form;
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        SubscriptionForm form = createValidForm();

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankHost_ShouldHaveViolation() {
        SubscriptionForm form = createValidForm();
        form.setHost("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("host"));
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        SubscriptionForm form = createValidForm();
        form.setUsername("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankType_ShouldHaveViolation() {
        SubscriptionForm form = createValidForm();
        form.setType("");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    void invalidType_ShouldHaveViolation() {
        SubscriptionForm form = createValidForm();
        form.setType("invalid");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("type"));
    }

    @Test
    void imapType_ShouldBeValid() {
        SubscriptionForm form = createValidForm();
        form.setType("imap");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void pop3Type_ShouldBeValid() {
        SubscriptionForm form = createValidForm();
        form.setType("pop3");

        Set<ConstraintViolation<SubscriptionForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        SubscriptionForm form = new SubscriptionForm();
        
        form.setAction("Edit");
        form.setHost("mail.example.com");
        form.setUsername("mailuser");
        form.setPassword("mailpass");
        form.setType("pop3");
        form.setAutoConnect(true);
        
        assertThat(form.getAction()).isEqualTo("Edit");
        assertThat(form.getHost()).isEqualTo("mail.example.com");
        assertThat(form.getUsername()).isEqualTo("mailuser");
        assertThat(form.getPassword()).isEqualTo("mailpass");
        assertThat(form.getType()).isEqualTo("pop3");
        assertThat(form.isAutoConnect()).isTrue();
    }

    @Test
    void defaultAction_ShouldBeCreate() {
        SubscriptionForm form = new SubscriptionForm();
        
        assertThat(form.getAction()).isEqualTo("Create");
    }

    @Test
    void defaultAutoConnect_ShouldBeFalse() {
        SubscriptionForm form = new SubscriptionForm();
        
        assertThat(form.isAutoConnect()).isFalse();
    }
}
