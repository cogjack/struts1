package org.apache.struts.webapp.example2.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.struts.webapp.example2.form.RegistrationForm;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegistrationForm> {

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
            return false;
        }
        return password.equals(password2);
    }
}
