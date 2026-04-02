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

import org.apache.struts.webapp.example2.config.DatabaseConfiguration;
import org.apache.struts.webapp.example2.controller.LogoffController;
import org.apache.struts.webapp.example2.controller.LogonController;
import org.apache.struts.webapp.example2.controller.RegistrationController;
import org.apache.struts.webapp.example2.controller.SubscriptionController;
import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 5.3 - Application Context Integration Test.
 * Verifies that the Spring context loads without errors and all controllers
 * and beans are properly initialized.
 */
@SpringBootTest
@Import(TestDatabaseConfiguration.class)
class ApplicationContextIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private List<DatabaseConfiguration.LabelValueBean> serverTypes;

    // --- Context loads ---

    @Test
    void contextLoadsSuccessfully() {
        assertThat(context).isNotNull();
    }

    // --- All controllers are loaded ---

    @Test
    void logonControllerIsLoaded() {
        assertThat(context.getBean(LogonController.class)).isNotNull();
    }

    @Test
    void logoffControllerIsLoaded() {
        assertThat(context.getBean(LogoffController.class)).isNotNull();
    }

    @Test
    void registrationControllerIsLoaded() {
        assertThat(context.getBean(RegistrationController.class)).isNotNull();
    }

    @Test
    void subscriptionControllerIsLoaded() {
        assertThat(context.getBean(SubscriptionController.class)).isNotNull();
    }

    // --- UserDatabase bean is initialized with pre-loaded data ---

    @Test
    void userDatabaseBeanIsInitialized() {
        assertThat(userDatabase).isNotNull();
    }

    @Test
    void userDatabaseContainsPreloadedUser() {
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.getFullName()).isEqualTo("John Q. User");
        assertThat(user.getFromAddress()).isEqualTo("John.User@somewhere.com");
    }

    @Test
    void userDatabaseContainsPreloadedSubscriptions() {
        User user = userDatabase.findUser("user");
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(2);

        Subscription yahoo = user.findSubscription("mail.yahoo.com");
        assertThat(yahoo).isNotNull();
        assertThat(yahoo.getType()).isEqualTo("imap");

        Subscription hotmail = user.findSubscription("mail.hotmail.com");
        assertThat(hotmail).isNotNull();
        assertThat(hotmail.getType()).isEqualTo("pop3");
    }

    // --- Configuration beans ---

    @Test
    void serverTypesBeanContainsImapAndPop3() {
        assertThat(serverTypes).hasSize(2);
        assertThat(serverTypes)
                .extracting(DatabaseConfiguration.LabelValueBean::getValue)
                .containsExactlyInAnyOrder("imap", "pop3");
    }

    @Test
    void databaseConfigurationBeanIsLoaded() {
        assertThat(context.getBean(DatabaseConfiguration.class)).isNotNull();
    }
}
