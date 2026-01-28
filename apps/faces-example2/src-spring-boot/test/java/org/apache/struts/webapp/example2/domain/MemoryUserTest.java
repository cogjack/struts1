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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoryUserTest {

    private MemoryUserDatabase database;
    private MemoryUser user;

    @BeforeEach
    void setUp() {
        database = new MemoryUserDatabase();
        user = new MemoryUser(database, "testuser");
    }

    @Test
    void constructor_ShouldSetDatabaseAndUsername() {
        assertThat(user.getDatabase()).isSameAs(database);
        assertThat(user.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getFromAddress_DefaultValue_ShouldBeNull() {
        assertThat(user.getFromAddress()).isNull();
    }

    @Test
    void setFromAddress_ShouldUpdateValue() {
        user.setFromAddress("test@example.com");
        assertThat(user.getFromAddress()).isEqualTo("test@example.com");
    }

    @Test
    void getFullName_DefaultValue_ShouldBeNull() {
        assertThat(user.getFullName()).isNull();
    }

    @Test
    void setFullName_ShouldUpdateValue() {
        user.setFullName("Test User");
        assertThat(user.getFullName()).isEqualTo("Test User");
    }

    @Test
    void getPassword_DefaultValue_ShouldBeNull() {
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void setPassword_ShouldUpdateValue() {
        user.setPassword("secret123");
        assertThat(user.getPassword()).isEqualTo("secret123");
    }

    @Test
    void getReplyToAddress_DefaultValue_ShouldBeNull() {
        assertThat(user.getReplyToAddress()).isNull();
    }

    @Test
    void setReplyToAddress_ShouldUpdateValue() {
        user.setReplyToAddress("reply@example.com");
        assertThat(user.getReplyToAddress()).isEqualTo("reply@example.com");
    }

    @Test
    void getSubscriptions_InitiallyEmpty_ShouldReturnEmptyArray() {
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).isEmpty();
    }

    @Test
    void createSubscription_ShouldCreateAndReturnSubscription() {
        Subscription subscription = user.createSubscription("mail.example.com");
        
        assertThat(subscription).isNotNull();
        assertThat(subscription.getHost()).isEqualTo("mail.example.com");
        assertThat(subscription.getUser()).isSameAs(user);
    }

    @Test
    void createSubscription_ShouldAddToSubscriptionsList() {
        user.createSubscription("mail.example.com");
        
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(1);
        assertThat(subscriptions[0].getHost()).isEqualTo("mail.example.com");
    }

    @Test
    void createSubscription_DuplicateHost_ShouldThrowException() {
        user.createSubscription("mail.example.com");
        
        assertThatThrownBy(() -> user.createSubscription("mail.example.com"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate host")
            .hasMessageContaining("mail.example.com");
    }

    @Test
    void findSubscription_ExistingHost_ShouldReturnSubscription() {
        user.createSubscription("mail.example.com");
        
        Subscription found = user.findSubscription("mail.example.com");
        assertThat(found).isNotNull();
        assertThat(found.getHost()).isEqualTo("mail.example.com");
    }

    @Test
    void findSubscription_NonExistingHost_ShouldReturnNull() {
        Subscription found = user.findSubscription("nonexistent.com");
        assertThat(found).isNull();
    }

    @Test
    void removeSubscription_ValidSubscription_ShouldRemove() {
        Subscription subscription = user.createSubscription("mail.example.com");
        assertThat(user.getSubscriptions()).hasSize(1);
        
        user.removeSubscription(subscription);
        assertThat(user.getSubscriptions()).isEmpty();
    }

    @Test
    void removeSubscription_SubscriptionFromDifferentUser_ShouldThrowException() {
        MemoryUser otherUser = new MemoryUser(database, "otheruser");
        Subscription otherSubscription = otherUser.createSubscription("mail.example.com");
        
        assertThatThrownBy(() -> user.removeSubscription(otherSubscription))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not associated with this user");
    }

    @Test
    void getSubscriptions_MultipleSubscriptions_ShouldReturnAll() {
        user.createSubscription("mail1.example.com");
        user.createSubscription("mail2.example.com");
        user.createSubscription("mail3.example.com");
        
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(3);
    }

    @Test
    void toString_ShouldContainUsername() {
        String result = user.toString();
        assertThat(result).contains("username=\"testuser\"");
    }

    @Test
    void toString_ShouldContainAllSetProperties() {
        user.setFromAddress("from@example.com");
        user.setFullName("Test User");
        user.setPassword("secret");
        user.setReplyToAddress("reply@example.com");
        
        String result = user.toString();
        assertThat(result)
            .contains("username=\"testuser\"")
            .contains("fromAddress=\"from@example.com\"")
            .contains("fullName=\"Test User\"")
            .contains("password=\"secret\"")
            .contains("replyToAddress=\"reply@example.com\"");
    }

    @Test
    void toString_ShouldStartWithUserTag() {
        String result = user.toString();
        assertThat(result).startsWith("<user ");
    }

    @Test
    void toString_ShouldEndWithClosingBracket() {
        String result = user.toString();
        assertThat(result).endsWith(">");
    }
}
