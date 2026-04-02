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

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.struts.webapp.example2.form.RegistrationForm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordMatchValidatorTest {

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
    void matchingPasswords_ShouldBeValid() {
        RegistrationForm form = createValidForm();
        form.setPassword("secret");
        form.setPassword2("secret");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("password2")
                && v.getMessage().contains("match"));
    }

    @Test
    void mismatchedPasswords_ShouldBeInvalid() {
        RegistrationForm form = createValidForm();
        form.setPassword("password1");
        form.setPassword2("password2");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void bothPasswordsNull_ShouldBeValid() {
        RegistrationForm form = createValidForm();
        form.setPassword(null);
        form.setPassword2(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void passwordNullPassword2NotNull_ShouldBeInvalid() {
        RegistrationForm form = createValidForm();
        form.setPassword(null);
        form.setPassword2("something");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void passwordNotNullPassword2Null_ShouldBeInvalid() {
        RegistrationForm form = createValidForm();
        form.setPassword("something");
        form.setPassword2(null);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password2"));
    }

    @Test
    void emptyMatchingPasswords_ShouldBeValid() {
        RegistrationForm form = createValidForm();
        form.setPassword("");
        form.setPassword2("");

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("password2")
                && v.getMessage().contains("match"));
    }
}
