package org.apache.struts.webapp.example2.form;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegistrationForm validForm() {
        RegistrationForm form = new RegistrationForm();
        form.setAction("Create");
        form.setUsername("testuser");
        form.setFullName("Test User");
        form.setFromAddress("test@example.com");
        form.setPassword("secret");
        form.setPassword2("secret");
        return form;
    }

    @Test
    void validFormHasNoViolations() {
        RegistrationForm form = validForm();
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void defaultActionIsCreate() {
        RegistrationForm form = new RegistrationForm();
        assertThat(form.getAction()).isEqualTo("Create");
    }

    @Test
    void blankUsernameViolation() {
        RegistrationForm form = validForm();
        form.setUsername("");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void nullUsernameViolation() {
        RegistrationForm form = validForm();
        form.setUsername(null);
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void blankFullNameViolation() {
        RegistrationForm form = validForm();
        form.setFullName("");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fullName"));
    }

    @Test
    void blankFromAddressViolation() {
        RegistrationForm form = validForm();
        form.setFromAddress("");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidFromAddressViolation() {
        RegistrationForm form = validForm();
        form.setFromAddress("not-an-email");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fromAddress"));
    }

    @Test
    void invalidReplyToAddressViolation() {
        RegistrationForm form = validForm();
        form.setReplyToAddress("bad-email");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("replyToAddress"));
    }

    @Test
    void validReplyToAddressAccepted() {
        RegistrationForm form = validForm();
        form.setReplyToAddress("reply@example.com");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void nullReplyToAddressAccepted() {
        RegistrationForm form = validForm();
        form.setReplyToAddress(null);
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void passwordMismatchViolation() {
        RegistrationForm form = validForm();
        form.setPassword("secret");
        form.setPassword2("different");
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bothPasswordsNullIsValid() {
        RegistrationForm form = validForm();
        form.setPassword(null);
        form.setPassword2(null);
        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);
        assertThat(violations).isEmpty();
    }

    @Test
    void gettersAndSettersWork() {
        RegistrationForm form = new RegistrationForm();
        form.setAction("Edit");
        form.setUsername("user1");
        form.setFullName("Full Name");
        form.setFromAddress("from@test.com");
        form.setPassword("pw");
        form.setPassword2("pw");
        form.setReplyToAddress("reply@test.com");

        assertThat(form.getAction()).isEqualTo("Edit");
        assertThat(form.getUsername()).isEqualTo("user1");
        assertThat(form.getFullName()).isEqualTo("Full Name");
        assertThat(form.getFromAddress()).isEqualTo("from@test.com");
        assertThat(form.getPassword()).isEqualTo("pw");
        assertThat(form.getPassword2()).isEqualTo("pw");
        assertThat(form.getReplyToAddress()).isEqualTo("reply@test.com");
    }
}
