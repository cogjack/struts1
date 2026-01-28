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
package org.apache.struts.webapp.example2.springmvc.config;

import java.util.Arrays;
import java.util.List;

import org.apache.struts.webapp.example2.UserDatabase;
import org.apache.struts.webapp.example2.memory.MemoryUserDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PreDestroy;

@Configuration
public class DatabaseConfiguration {

    @Value("${app.database.path:classpath:database.xml}")
    private Resource databasePath;

    private MemoryUserDatabase database;

    @Bean
    public UserDatabase userDatabase() throws Exception {
        database = new MemoryUserDatabase();
        if (databasePath != null && databasePath.exists()) {
            database.setPathname(databasePath.getFile().getAbsolutePath());
        }
        database.open();
        return database;
    }

    @Bean
    public List<ServerType> serverTypes() {
        return Arrays.asList(
            new ServerType("IMAP Protocol", "imap"),
            new ServerType("POP3 Protocol", "pop3")
        );
    }

    @PreDestroy
    public void cleanup() {
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                // Log error during cleanup
            }
        }
    }

    public static class ServerType {
        private final String label;
        private final String value;

        public ServerType(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }
}
