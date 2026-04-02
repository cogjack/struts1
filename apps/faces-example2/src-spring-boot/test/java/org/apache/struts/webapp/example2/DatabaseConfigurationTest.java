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
import org.apache.struts.webapp.example2.domain.MemoryUserDatabase;
import org.apache.struts.webapp.example2.domain.Subscription;
import org.apache.struts.webapp.example2.domain.User;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the real DatabaseConfiguration bean loads from the classpath
 * database.xml resource. Unlike DatabaseIntegrationTest (which uses
 * TestDatabaseConfiguration), this test validates the production
 * configuration path.
 */
@SpringBootTest
@TestPropertySource(properties = "app.database.path=classpath:database.xml")
class DatabaseConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void databaseConfigurationBean_ShouldBeLoaded() {
        assertThat(applicationContext.getBean(DatabaseConfiguration.class)).isNotNull();
    }

    @Test
    void userDatabaseBean_ShouldBeProvided() {
        assertThat(userDatabase).isNotNull();
    }

    @Test
    void userDatabase_ShouldLoadUsersFromXml() {
        User[] users = userDatabase.findUsers();
        assertThat(users).isNotEmpty();
    }

    @Test
    void userDatabase_ShouldContainSeedUser() {
        // database.xml contains a user "user" with password "pass"
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        assertThat(user.getPassword()).isEqualTo("pass");
    }

    @Test
    void userDatabase_SeedUserShouldHaveSubscriptions() {
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).isNotEmpty();
    }

    @Test
    void serverTypesBean_ShouldBeProvided() {
        @SuppressWarnings("unchecked")
        List<DatabaseConfiguration.LabelValueBean> serverTypes =
            (List<DatabaseConfiguration.LabelValueBean>) applicationContext.getBean("serverTypes");
        assertThat(serverTypes).isNotNull();
        assertThat(serverTypes).isNotEmpty();
    }

    @Test
    void serverTypesBean_ShouldContainImapAndPop3() {
        @SuppressWarnings("unchecked")
        List<DatabaseConfiguration.LabelValueBean> serverTypes =
            (List<DatabaseConfiguration.LabelValueBean>) applicationContext.getBean("serverTypes");

        assertThat(serverTypes)
            .extracting(DatabaseConfiguration.LabelValueBean::getValue)
            .contains("imap", "pop3");
    }

    @Test
    void dataPersistence_SaveAndReload_ShouldPreserveData(@TempDir Path tempDir) throws Exception {
        // Phase 2 acceptance criteria: "Verify data persistence across
        // application restarts" — we simulate this by loading the classpath
        // database.xml into a standalone MemoryUserDatabase, adding a user,
        // saving to disk, then creating a fresh instance and reopening.
        String dbPath = tempDir.resolve("database.xml").toString();

        MemoryUserDatabase db1 = new MemoryUserDatabase();
        db1.setPathname(dbPath);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.xml")) {
            db1.open(is);
        }

        String testUsername = "persistence_test_" + System.currentTimeMillis();
        User newUser = db1.createUser(testUsername);
        newUser.setPassword("persist_pass");
        newUser.setFullName("Persistence Test User");

        Subscription sub = newUser.createSubscription("mail.persist.com");
        sub.setType("pop3");
        sub.setUsername("persistuser");
        sub.setPassword("persistpwd");
        sub.setAutoConnect(true);

        db1.save();

        // Create a fresh instance and load from the saved file
        MemoryUserDatabase db2 = new MemoryUserDatabase();
        db2.setPathname(dbPath);
        db2.open();

        // Verify seed user survived the round-trip
        User seedUser = db2.findUser("user");
        assertThat(seedUser).isNotNull();
        assertThat(seedUser.getPassword()).isEqualTo("pass");

        // Verify newly created user was persisted
        User reloadedUser = db2.findUser(testUsername);
        assertThat(reloadedUser).isNotNull();
        assertThat(reloadedUser.getPassword()).isEqualTo("persist_pass");
        assertThat(reloadedUser.getFullName()).isEqualTo("Persistence Test User");

        // Verify persisted subscription
        Subscription reloadedSub = reloadedUser.findSubscription("mail.persist.com");
        assertThat(reloadedSub).isNotNull();
        assertThat(reloadedSub.getType()).isEqualTo("pop3");
        assertThat(reloadedSub.getUsername()).isEqualTo("persistuser");
        assertThat(reloadedSub.getPassword()).isEqualTo("persistpwd");
        assertThat(reloadedSub.getAutoConnect()).isTrue();
    }
}
