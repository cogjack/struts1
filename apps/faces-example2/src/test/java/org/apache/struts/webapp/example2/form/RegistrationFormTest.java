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

class RegistrationFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegistrationForm createValidForm() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setPassword("password123");
        form.setPassword2("password123");
        return form;
    }

    @Test
    void validForm_ShouldHaveNoViolations() {
        RegistrationForm form = createValidForm();

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setUsername("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankFullName_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setFullName("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void blankFromAddress_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setFromAddress("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidFromAddressEmail_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setFromAddress("not-an-email");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidReplyToAddressEmail_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setReplyToAddress("not-an-email");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("replyToAddress"));
    }

    @Test
    void validReplyToAddress_ShouldHaveNoViolation() {
        RegistrationForm form = createValidForm();
        form.setReplyToAddress("reply@example.com");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void passwordMismatch_ShouldHaveViolation() {
        RegistrationForm form = createValidForm();
        form.setPassword("password123");
        form.setPassword2("differentpassword");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        RegistrationForm form = new RegistrationForm();
        
        form.setAction("Edit");
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setReplyToAddress("reply@example.com");
        form.setPassword("password");
        form.setPassword2("password");
        
        assertThat(form.getAction()).isEqualTo("Edit");
        assertThat(form.getUsername()).isEqualTo("testuser");
        assertThat(form.getFullName()).isEqualTo("Test User");
        assertThat(form.getFromAddress()).isEqualTo("test@example.com");
        assertThat(form.getReplyToAddress()).isEqualTo("reply@example.com");
        assertThat(form.getPassword()).isEqualTo("password");
        assertThat(form.getPassword2()).isEqualTo("password");
    }

    @Test
    void defaultAction_ShouldBeCreate() {
        RegistrationForm form = new RegistrationForm();
        
        assertThat(form.getAction()).isEqualTo("Create");
    }
}
