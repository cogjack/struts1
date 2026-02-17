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
package org.apache.struts.webapp.example2;

import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestDatabaseConfiguration.class)
class DatabaseIntegrationTest {

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void databaseIsInitialized() {
        assertThat(userDatabase).isNotNull();
    }

    @Test
    void initialDataIsLoaded() {
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
    }

    @Test
    void initialUserHasCorrectPassword() {
        User user = userDatabase.findUser("user");
        assertThat(user.getPassword()).isEqualTo("pass");
    }

    @Test
    void initialUserHasCorrectFullName() {
        User user = userDatabase.findUser("user");
        assertThat(user.getFullName()).isEqualTo("John Q. User");
    }

    @Test
    void initialUserHasCorrectFromAddress() {
        User user = userDatabase.findUser("user");
        assertThat(user.getFromAddress()).isEqualTo("John.User@somewhere.com");
    }

    @Test
    void initialUserHasSubscriptions() {
        User user = userDatabase.findUser("user");
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(2);
    }

    @Test
    void initialUserHasYahooSubscription() {
        User user = userDatabase.findUser("user");
        Subscription subscription = user.findSubscription("mail.yahoo.com");

        assertThat(subscription).isNotNull();
        assertThat(subscription.getType()).isEqualTo("imap");
        assertThat(subscription.getUsername()).isEqualTo("jquser");
        assertThat(subscription.getPassword()).isEqualTo("foo");
    }

    @Test
    void initialUserHasHotmailSubscription() {
        User user = userDatabase.findUser("user");
        Subscription subscription = user.findSubscription("mail.hotmail.com");

        assertThat(subscription).isNotNull();
        assertThat(subscription.getType()).isEqualTo("pop3");
        assertThat(subscription.getUsername()).isEqualTo("user1234");
        assertThat(subscription.getPassword()).isEqualTo("bar");
    }

    @Test
    void createUser_ShouldAddNewUser() {
        String newUsername = "newuser_" + System.currentTimeMillis();
        User newUser = userDatabase.createUser(newUsername);

        assertThat(newUser).isNotNull();
        assertThat(newUser.getUsername()).isEqualTo(newUsername);

        User foundUser = userDatabase.findUser(newUsername);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo(newUsername);
    }

    @Test
    void createUser_WithProperties_ShouldPersistProperties() {
        String newUsername = "propsuser_" + System.currentTimeMillis();
        User newUser = userDatabase.createUser(newUsername);
        newUser.setPassword("newpass");
        newUser.setFullName("New User");
        newUser.setFromAddress("new@example.com");
        newUser.setReplyToAddress("reply@example.com");

        User foundUser = userDatabase.findUser(newUsername);
        assertThat(foundUser.getPassword()).isEqualTo("newpass");
        assertThat(foundUser.getFullName()).isEqualTo("New User");
        assertThat(foundUser.getFromAddress()).isEqualTo("new@example.com");
        assertThat(foundUser.getReplyToAddress()).isEqualTo("reply@example.com");
    }

    @Test
    void createUser_DuplicateUsername_ShouldThrowException() {
        assertThatThrownBy(() -> userDatabase.createUser("user"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate user");
    }

    @Test
    void findUser_NonExistingUser_ShouldReturnNull() {
        User user = userDatabase.findUser("nonexistent_user_xyz");
        assertThat(user).isNull();
    }

    @Test
    void findUsers_ShouldReturnAllUsers() {
        User[] users = userDatabase.findUsers();
        assertThat(users).isNotEmpty();
        assertThat(users.length).isGreaterThanOrEqualTo(1);
    }

    @Test
    void removeUser_ShouldRemoveUser() {
        String usernameToRemove = "removeuser_" + System.currentTimeMillis();
        User user = userDatabase.createUser(usernameToRemove);
        assertThat(userDatabase.findUser(usernameToRemove)).isNotNull();

        userDatabase.removeUser(user);
        assertThat(userDatabase.findUser(usernameToRemove)).isNull();
    }

    @Test
    void createSubscription_ShouldAddSubscription() {
        String newUsername = "subuser_" + System.currentTimeMillis();
        User user = userDatabase.createUser(newUsername);

        Subscription subscription = user.createSubscription("mail.test.com");
        subscription.setType("imap");
        subscription.setUsername("testmail");
        subscription.setPassword("testpass");
        subscription.setAutoConnect(true);

        Subscription foundSub = user.findSubscription("mail.test.com");
        assertThat(foundSub).isNotNull();
        assertThat(foundSub.getType()).isEqualTo("imap");
        assertThat(foundSub.getUsername()).isEqualTo("testmail");
        assertThat(foundSub.getPassword()).isEqualTo("testpass");
        assertThat(foundSub.getAutoConnect()).isTrue();
    }

    @Test
    void createSubscription_DuplicateHost_ShouldThrowException() {
        User user = userDatabase.findUser("user");

        assertThatThrownBy(() -> user.createSubscription("mail.yahoo.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate host");
    }

    @Test
    void removeSubscription_ShouldRemoveSubscription() {
        String newUsername = "removesub_" + System.currentTimeMillis();
        User user = userDatabase.createUser(newUsername);

        Subscription subscription = user.createSubscription("mail.remove.com");
        assertThat(user.findSubscription("mail.remove.com")).isNotNull();

        user.removeSubscription(subscription);
        assertThat(user.findSubscription("mail.remove.com")).isNull();
    }

    @Test
    void updateSubscription_ShouldPersistChanges() {
        String newUsername = "updatesub_" + System.currentTimeMillis();
        User user = userDatabase.createUser(newUsername);

        Subscription subscription = user.createSubscription("mail.update.com");
        subscription.setType("imap");
        subscription.setUsername("original");

        subscription.setType("pop3");
        subscription.setUsername("updated");

        Subscription foundSub = user.findSubscription("mail.update.com");
        assertThat(foundSub.getType()).isEqualTo("pop3");
        assertThat(foundSub.getUsername()).isEqualTo("updated");
    }

    @Test
    void updateUser_ShouldPersistChanges() {
        String newUsername = "updateuser_" + System.currentTimeMillis();
        User user = userDatabase.createUser(newUsername);
        user.setFullName("Original Name");

        user.setFullName("Updated Name");

        User foundUser = userDatabase.findUser(newUsername);
        assertThat(foundUser.getFullName()).isEqualTo("Updated Name");
    }
}
