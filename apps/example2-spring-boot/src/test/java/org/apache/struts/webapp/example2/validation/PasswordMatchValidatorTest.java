package org.apache.struts.webapp.example2.validation;

import org.apache.struts.webapp.example2.form.RegistrationForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordMatchValidatorTest {

    private PasswordMatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordMatchValidator();
    }

    @Test
    void nullFormIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void bothPasswordsNullIsValid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword(null);
        form.setPassword2(null);
        assertThat(validator.isValid(form, null)).isTrue();
    }

    @Test
    void matchingPasswordsAreValid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("secret");
        form.setPassword2("secret");
        assertThat(validator.isValid(form, null)).isTrue();
    }

    @Test
    void mismatchedPasswordsAreInvalid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("secret");
        form.setPassword2("different");
        assertThat(validator.isValid(form, null)).isFalse();
    }

    @Test
    void firstPasswordNullSecondNotNullIsInvalid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword(null);
        form.setPassword2("something");
        assertThat(validator.isValid(form, null)).isFalse();
    }

    @Test
    void firstPasswordNotNullSecondNullIsInvalid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("something");
        form.setPassword2(null);
        assertThat(validator.isValid(form, null)).isFalse();
    }

    @Test
    void emptyMatchingPasswordsAreValid() {
        RegistrationForm form = new RegistrationForm();
        form.setPassword("");
        form.setPassword2("");
        assertThat(validator.isValid(form, null)).isTrue();
    }
}
