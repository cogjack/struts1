package org.apache.struts.webapp.example2.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.apache.struts.webapp.example2.validation.PasswordMatch;

@PasswordMatch
public class RegistrationForm {

    private String action = "Create";

    @NotBlank(message = "{error.fromAddress.required}")
    @Email(message = "{error.fromAddress.format}")
    private String fromAddress;

    @NotBlank(message = "{error.fullName.required}")
    private String fullName;

    private String password;

    private String password2;

    @Email(message = "{error.replyToAddress.format}")
    private String replyToAddress;

    @NotBlank(message = "{error.username.required}")
    private String username;

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFromAddress() {
        return this.fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return this.password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getReplyToAddress() {
        return this.replyToAddress;
    }

    public void setReplyToAddress(String replyToAddress) {
        this.replyToAddress = replyToAddress;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
