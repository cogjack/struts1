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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.struts.webapp.example2.form.RegistrationForm;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegistrationForm> {

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
    }

    @Override
    public boolean isValid(RegistrationForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }

        String password = form.getPassword();
        String password2 = form.getPassword2();

        if (password == null && password2 == null) {
            return true;
        }

        if (password == null || password2 == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{error.password.match}")
                   .addPropertyNode("password2")
                   .addConstraintViolation();
            return false;
        }

        if (!password.equals(password2)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("{error.password.match}")
                   .addPropertyNode("password2")
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
