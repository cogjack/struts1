package org.apache.struts.webapp.example2.controller;

import org.apache.struts.webapp.example2.Constants;
import org.apache.struts.webapp.example2.TestDatabaseConfiguration;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfiguration.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void showRegistrationFormForNewUser() throws Exception {
        mockMvc.perform(get("/editRegistration"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void showRegistrationFormForNewUserExplicitAction() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Create"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void showRegistrationFormForExistingUser() throws Exception {
        User user = userDatabase.findUser("user");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(get("/editRegistration")
                        .param("action", "Edit")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeExists("registrationForm"));
    }

    @Test
    void editRegistrationRedirectsToLogonWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/editRegistration").param("action", "Edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logon"));
    }

    @Test
    void createNewUserWithValidData() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/saveRegistration")
                        .session(session)
                        .param("action", "Create")
                        .param("username", "newuser")
                        .param("fullName", "New User")
                        .param("fromAddress", "new@example.com")
                        .param("password", "newpass")
                        .param("password2", "newpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));

        assertThat(session.getAttribute(Constants.USER_KEY)).isNotNull();
        User created = userDatabase.findUser("newuser");
        assertThat(created).isNotNull();
        assertThat(created.getFullName()).isEqualTo("New User");
        assertThat(created.getFromAddress()).isEqualTo("new@example.com");
        assertThat(created.getPassword()).isEqualTo("newpass");
    }

    @Test
    void editExistingUserWithValidData() throws Exception {
        User user = userDatabase.findUser("user");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                        .session(session)
                        .param("action", "Edit")
                        .param("username", "user")
                        .param("fullName", "Updated Name")
                        .param("fromAddress", "updated@example.com")
                        .param("password", "")
                        .param("password2", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));

        assertThat(user.getFullName()).isEqualTo("Updated Name");
        assertThat(user.getFromAddress()).isEqualTo("updated@example.com");
        assertThat(user.getPassword()).isEqualTo("pass");
    }

    @Test
    void createDuplicateUsernameShowsError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "user")
                        .param("fullName", "Dup User")
                        .param("fromAddress", "dup@example.com")
                        .param("password", "pw")
                        .param("password2", "pw"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "username"));
    }

    @Test
    void createWithBlankFieldsShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "")
                        .param("fullName", "")
                        .param("fromAddress", "")
                        .param("password", "")
                        .param("password2", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm",
                        "username", "fullName", "fromAddress"));
    }

    @Test
    void createWithInvalidEmailShowsValidationErrors() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "emailtest")
                        .param("fullName", "Email Test")
                        .param("fromAddress", "invalid-email")
                        .param("password", "pw")
                        .param("password2", "pw"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "fromAddress"));
    }

    @Test
    void createWithPasswordMismatchShowsError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "mismatchuser")
                        .param("fullName", "Mismatch User")
                        .param("fromAddress", "mm@example.com")
                        .param("password", "secret")
                        .param("password2", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasErrors("registrationForm"));
    }

    @Test
    void saveRegistrationRedirectsToLogonWhenNotLoggedInAndEditAction() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Edit")
                        .param("username", "user")
                        .param("fullName", "Test")
                        .param("fromAddress", "t@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logon"));
    }

    @Test
    void createWithMissingPasswordShowsError() throws Exception {
        mockMvc.perform(post("/saveRegistration")
                        .param("action", "Create")
                        .param("username", "nopwuser")
                        .param("fullName", "No PW")
                        .param("fromAddress", "nopw@example.com")
                        .param("password", "")
                        .param("password2", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attributeHasFieldErrors("registrationForm", "password", "password2"));
    }

    @Test
    void editExistingUserPreservesPasswordWhenBlank() throws Exception {
        User user = userDatabase.findUser("user");
        String originalPassword = user.getPassword();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(Constants.USER_KEY, user);

        mockMvc.perform(post("/saveRegistration")
                        .session(session)
                        .param("action", "Edit")
                        .param("username", "user")
                        .param("fullName", "Keep Pass")
                        .param("fromAddress", "keep@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mainMenu"));

        assertThat(user.getPassword()).isEqualTo(originalPassword);
    }
}
