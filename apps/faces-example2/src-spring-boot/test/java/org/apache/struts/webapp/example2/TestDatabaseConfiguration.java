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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Test configuration that provides a pre-populated in-memory database
 * for integration testing. This replaces the XML-based database loading
 * with programmatic test data setup.
 */
@TestConfiguration
public class TestDatabaseConfiguration {

    /**
     * Creates a UserDatabase bean pre-populated with sample test data.
     * This data mirrors the original database.xml from the Struts application.
     */
    @Bean
    @Primary
    public UserDatabase testUserDatabase() {
        MemoryUserDatabase database = new MemoryUserDatabase();
        
        User user = database.createUser("user");
        user.setPassword("pass");
        user.setFullName("John Q. User");
        user.setFromAddress("John.User@somewhere.com");
        
        Subscription yahooSub = user.createSubscription("mail.yahoo.com");
        yahooSub.setType("imap");
        yahooSub.setUsername("jquser");
        yahooSub.setPassword("foo");
        
        Subscription hotmailSub = user.createSubscription("mail.hotmail.com");
        hotmailSub.setType("pop3");
        hotmailSub.setUsername("user1234");
        hotmailSub.setPassword("bar");
        
        return database;
    }

    /**
     * Provides server types for subscription forms.
     */
    @Bean
    @Primary
    public List<DatabaseConfiguration.LabelValueBean> testServerTypes() {
        return List.of(
            new DatabaseConfiguration.LabelValueBean("IMAP Protocol", "imap"),
            new DatabaseConfiguration.LabelValueBean("POP3 Protocol", "pop3")
        );
    }
}
