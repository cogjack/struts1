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
package org.apache.struts.webapp.example2.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemorySubscriptionTest {

    private MemoryUserDatabase database;
    private MemoryUser user;
    private MemorySubscription subscription;

    @BeforeEach
    void setUp() {
        database = new MemoryUserDatabase();
        user = new MemoryUser(database, "testuser");
        subscription = new MemorySubscription(user, "mail.example.com");
    }

    @Test
    void constructor_ShouldSetHostAndUser() {
        assertThat(subscription.getHost()).isEqualTo("mail.example.com");
        assertThat(subscription.getUser()).isSameAs(user);
    }

    @Test
    void getAutoConnect_DefaultValue_ShouldBeFalse() {
        assertThat(subscription.getAutoConnect()).isFalse();
    }

    @Test
    void setAutoConnect_ShouldUpdateValue() {
        subscription.setAutoConnect(true);
        assertThat(subscription.getAutoConnect()).isTrue();

        subscription.setAutoConnect(false);
        assertThat(subscription.getAutoConnect()).isFalse();
    }

    @Test
    void getType_DefaultValue_ShouldBeImap() {
        assertThat(subscription.getType()).isEqualTo("imap");
    }

    @Test
    void setType_ShouldUpdateValue() {
        subscription.setType("pop3");
        assertThat(subscription.getType()).isEqualTo("pop3");
    }

    @Test
    void getPassword_DefaultValue_ShouldBeNull() {
        assertThat(subscription.getPassword()).isNull();
    }

    @Test
    void setPassword_ShouldUpdateValue() {
        subscription.setPassword("secret123");
        assertThat(subscription.getPassword()).isEqualTo("secret123");
    }

    @Test
    void getUsername_DefaultValue_ShouldBeNull() {
        assertThat(subscription.getUsername()).isNull();
    }

    @Test
    void setUsername_ShouldUpdateValue() {
        subscription.setUsername("mailuser");
        assertThat(subscription.getUsername()).isEqualTo("mailuser");
    }

    @Test
    void toString_ShouldContainHost() {
        String result = subscription.toString();
        assertThat(result).contains("host=\"mail.example.com\"");
    }

    @Test
    void toString_ShouldContainAutoConnect() {
        subscription.setAutoConnect(true);
        String result = subscription.toString();
        assertThat(result).contains("autoConnect=\"true\"");
    }

    @Test
    void toString_ShouldContainAllSetProperties() {
        subscription.setAutoConnect(true);
        subscription.setPassword("secret");
        subscription.setType("pop3");
        subscription.setUsername("mailuser");

        String result = subscription.toString();
        assertThat(result)
            .contains("host=\"mail.example.com\"")
            .contains("autoConnect=\"true\"")
            .contains("password=\"secret\"")
            .contains("type=\"pop3\"")
            .contains("username=\"mailuser\"");
    }

    @Test
    void toString_ShouldStartWithSubscriptionTag() {
        String result = subscription.toString();
        assertThat(result).startsWith("<subscription ");
    }

    @Test
    void toString_ShouldEndWithClosingBracket() {
        String result = subscription.toString();
        assertThat(result).endsWith(">");
    }
}
