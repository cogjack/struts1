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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the real database.xml loading path
 * without the TestDatabaseConfiguration override. This ensures that
 * the production DatabaseConfiguration bean correctly parses the
 * classpath database.xml file via MemoryUserDatabase.
 */
@SpringBootTest
class DatabaseXmlLoadingTest {

    @Autowired
    private UserDatabase userDatabase;

    @Test
    void databaseLoadsFromClasspathXml() {
        assertThat(userDatabase).isNotNull();
    }

    @Test
    void preloadedUserExists() {
        User user = userDatabase.findUser("user");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("user");
    }

    @Test
    void preloadedUserHasCorrectAttributes() {
        User user = userDatabase.findUser("user");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.getFullName()).isEqualTo("John Q. User");
        assertThat(user.getFromAddress()).isEqualTo("John.User@somewhere.com");
    }

    @Test
    void preloadedUserHasSubscriptions() {
        User user = userDatabase.findUser("user");
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(2);
    }

    @Test
    void yahooSubscriptionLoadedCorrectly() {
        User user = userDatabase.findUser("user");
        Subscription yahoo = user.findSubscription("mail.yahoo.com");
        assertThat(yahoo).isNotNull();
        assertThat(yahoo.getType()).isEqualTo("imap");
        assertThat(yahoo.getUsername()).isEqualTo("jquser");
        assertThat(yahoo.getPassword()).isEqualTo("foo");
    }

    @Test
    void hotmailSubscriptionLoadedCorrectly() {
        User user = userDatabase.findUser("user");
        Subscription hotmail = user.findSubscription("mail.hotmail.com");
        assertThat(hotmail).isNotNull();
        assertThat(hotmail.getType()).isEqualTo("pop3");
        assertThat(hotmail.getUsername()).isEqualTo("user1234");
        assertThat(hotmail.getPassword()).isEqualTo("bar");
    }

    @Test
    void nonExistentUserReturnsNull() {
        User user = userDatabase.findUser("nonexistent");
        assertThat(user).isNull();
    }
}
