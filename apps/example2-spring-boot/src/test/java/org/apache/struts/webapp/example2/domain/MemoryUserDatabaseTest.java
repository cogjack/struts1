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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoryUserDatabaseTest {

    private MemoryUserDatabase database;

    @BeforeEach
    void setUp() {
        database = new MemoryUserDatabase();
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        User user = database.createUser("testuser");

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getDatabase()).isSameAs(database);
    }

    @Test
    void createUser_DuplicateUsername_ShouldThrowException() {
        database.createUser("testuser");

        assertThatThrownBy(() -> database.createUser("testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate user")
            .hasMessageContaining("testuser");
    }

    @Test
    void findUser_ExistingUser_ShouldReturnUser() {
        database.createUser("testuser");

        User found = database.findUser("testuser");
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser");
    }

    @Test
    void findUser_NonExistingUser_ShouldReturnNull() {
        User found = database.findUser("nonexistent");
        assertThat(found).isNull();
    }

    @Test
    void findUsers_EmptyDatabase_ShouldReturnEmptyArray() {
        User[] users = database.findUsers();
        assertThat(users).isEmpty();
    }

    @Test
    void findUsers_MultipleUsers_ShouldReturnAll() {
        database.createUser("user1");
        database.createUser("user2");
        database.createUser("user3");

        User[] users = database.findUsers();
        assertThat(users).hasSize(3);
    }

    @Test
    void removeUser_ValidUser_ShouldRemove() {
        User user = database.createUser("testuser");
        assertThat(database.findUsers()).hasSize(1);

        database.removeUser(user);
        assertThat(database.findUsers()).isEmpty();
        assertThat(database.findUser("testuser")).isNull();
    }

    @Test
    void removeUser_UserFromDifferentDatabase_ShouldThrowException() {
        MemoryUserDatabase otherDatabase = new MemoryUserDatabase();
        User otherUser = otherDatabase.createUser("otheruser");

        assertThatThrownBy(() -> database.removeUser(otherUser))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not associated with this database");
    }

    @Test
    void setPathname_ShouldSetPathnameAndDerivedPaths() {
        database.setPathname("/path/to/database.xml");
        assertThat(database.getPathname()).isEqualTo("/path/to/database.xml");
    }

    @Test
    void open_WithInputStream_ShouldLoadUsers() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<database>\n" +
                "  <user username=\"testuser\" password=\"testpass\" fullName=\"Test User\" fromAddress=\"test@example.com\">\n" +
                "    <subscription host=\"mail.example.com\" type=\"imap\" username=\"mailuser\" password=\"mailpass\"/>\n" +
                "  </user>\n" +
                "</database>";

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        database.open(is);

        User user = database.findUser("testuser");
        assertThat(user).isNotNull();
        assertThat(user.getPassword()).isEqualTo("testpass");
        assertThat(user.getFullName()).isEqualTo("Test User");
        assertThat(user.getFromAddress()).isEqualTo("test@example.com");
    }

    @Test
    void open_WithInputStream_ShouldLoadSubscriptions() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<database>\n" +
                "  <user username=\"testuser\" password=\"testpass\">\n" +
                "    <subscription host=\"mail.example.com\" type=\"imap\" username=\"mailuser\" password=\"mailpass\" autoConnect=\"true\"/>\n" +
                "    <subscription host=\"mail2.example.com\" type=\"pop3\" username=\"mailuser2\" password=\"mailpass2\"/>\n" +
                "  </user>\n" +
                "</database>";

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        database.open(is);

        User user = database.findUser("testuser");
        Subscription[] subscriptions = user.getSubscriptions();
        assertThat(subscriptions).hasSize(2);

        Subscription sub1 = user.findSubscription("mail.example.com");
        assertThat(sub1).isNotNull();
        assertThat(sub1.getType()).isEqualTo("imap");
        assertThat(sub1.getUsername()).isEqualTo("mailuser");
        assertThat(sub1.getPassword()).isEqualTo("mailpass");
        assertThat(sub1.getAutoConnect()).isTrue();

        Subscription sub2 = user.findSubscription("mail2.example.com");
        assertThat(sub2).isNotNull();
        assertThat(sub2.getType()).isEqualTo("pop3");
        assertThat(sub2.getAutoConnect()).isFalse();
    }

    @Test
    void open_WithMultipleUsers_ShouldLoadAll() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<database>\n" +
                "  <user username=\"user1\" password=\"pass1\"/>\n" +
                "  <user username=\"user2\" password=\"pass2\"/>\n" +
                "  <user username=\"user3\" password=\"pass3\"/>\n" +
                "</database>";

        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        database.open(is);

        assertThat(database.findUsers()).hasSize(3);
        assertThat(database.findUser("user1")).isNotNull();
        assertThat(database.findUser("user2")).isNotNull();
        assertThat(database.findUser("user3")).isNotNull();
    }

    @Test
    void save_ShouldWriteToFile(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("database.xml").toFile();
        database.setPathname(dbFile.getAbsolutePath());

        User user = database.createUser("testuser");
        user.setPassword("testpass");
        user.setFullName("Test User");
        user.setFromAddress("test@example.com");

        Subscription sub = user.createSubscription("mail.example.com");
        sub.setType("imap");
        sub.setUsername("mailuser");
        sub.setPassword("mailpass");

        database.save();

        assertThat(dbFile).exists();
        String content = Files.readString(dbFile.toPath());
        assertThat(content)
            .contains("testuser")
            .contains("testpass")
            .contains("Test User")
            .contains("mail.example.com");
    }

    @Test
    void close_ShouldSaveDatabase(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("database.xml").toFile();
        database.setPathname(dbFile.getAbsolutePath());

        database.createUser("testuser");
        database.close();

        assertThat(dbFile).exists();
    }

    @Test
    void open_FromFile_ShouldLoadDatabase(@TempDir Path tempDir) throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<database>\n" +
                "  <user username=\"fileuser\" password=\"filepass\"/>\n" +
                "</database>";

        File dbFile = tempDir.resolve("database.xml").toFile();
        Files.writeString(dbFile.toPath(), xml);

        database.setPathname(dbFile.getAbsolutePath());
        database.open();

        User user = database.findUser("fileuser");
        assertThat(user).isNotNull();
        assertThat(user.getPassword()).isEqualTo("filepass");
    }

    @Test
    void roundTrip_SaveAndLoad_ShouldPreserveData(@TempDir Path tempDir) throws Exception {
        File dbFile = tempDir.resolve("database.xml").toFile();
        database.setPathname(dbFile.getAbsolutePath());

        User user = database.createUser("roundtripuser");
        user.setPassword("roundtrippass");
        user.setFullName("Round Trip User");
        user.setFromAddress("roundtrip@example.com");
        user.setReplyToAddress("reply@example.com");

        Subscription sub = user.createSubscription("mail.roundtrip.com");
        sub.setType("pop3");
        sub.setUsername("rtuser");
        sub.setPassword("rtpass");
        sub.setAutoConnect(true);

        database.save();

        MemoryUserDatabase loadedDb = new MemoryUserDatabase();
        loadedDb.setPathname(dbFile.getAbsolutePath());
        loadedDb.open();

        User loadedUser = loadedDb.findUser("roundtripuser");
        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getPassword()).isEqualTo("roundtrippass");
        assertThat(loadedUser.getFullName()).isEqualTo("Round Trip User");
        assertThat(loadedUser.getFromAddress()).isEqualTo("roundtrip@example.com");
        assertThat(loadedUser.getReplyToAddress()).isEqualTo("reply@example.com");

        Subscription loadedSub = loadedUser.findSubscription("mail.roundtrip.com");
        assertThat(loadedSub).isNotNull();
        assertThat(loadedSub.getType()).isEqualTo("pop3");
        assertThat(loadedSub.getUsername()).isEqualTo("rtuser");
        assertThat(loadedSub.getPassword()).isEqualTo("rtpass");
        assertThat(loadedSub.getAutoConnect()).isTrue();
    }
}
