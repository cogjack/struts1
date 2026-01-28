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
package org.apache.struts.webapp.example2.config;

import jakarta.annotation.PreDestroy;
import org.apache.struts.webapp.example2.domain.MemoryUserDatabase;
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Spring configuration class that replaces the MemoryDatabasePlugIn from Struts.
 * This class initializes the in-memory user database from an XML file and provides
 * it as a Spring bean for dependency injection.
 */
@Configuration
public class DatabaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Value("${app.database.path:classpath:database.xml}")
    private Resource databaseResource;

    private MemoryUserDatabase database;

    /**
     * Creates and initializes the UserDatabase bean.
     * The database is loaded from the configured XML file path.
     *
     * @return The initialized UserDatabase instance
     * @throws Exception if the database cannot be loaded
     */
    @Bean
    public UserDatabase userDatabase() throws Exception {
        log.info("Initializing memory database from '{}'", databaseResource);
        
        database = new MemoryUserDatabase();
        
        try {
            if (databaseResource.isFile()) {
                File file = databaseResource.getFile();
                database.setPathname(file.getAbsolutePath());
                database.open();
            } else {
                File tempFile = createTempDatabaseFile();
                database.setPathname(tempFile.getAbsolutePath());
                try (InputStream is = databaseResource.getInputStream()) {
                    database.open(is);
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize database from '{}': {}", databaseResource, e.getMessage());
            throw e;
        }
        
        log.info("Memory database initialized successfully");
        return database;
    }

    /**
     * Creates a temporary file for the database when running from a JAR.
     * This allows the database to be saved during runtime.
     *
     * @return The temporary file
     * @throws IOException if the file cannot be created
     */
    private File createTempDatabaseFile() throws IOException {
        File tempDir = Files.createTempDirectory("struts-example2").toFile();
        tempDir.deleteOnExit();
        File tempFile = new File(tempDir, "database.xml");
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Provides a list of server types for subscription forms.
     * This replaces the setupCache method from MemoryDatabasePlugIn.
     *
     * @return List of server type options
     */
    @Bean
    public List<LabelValueBean> serverTypes() {
        return List.of(
            new LabelValueBean("IMAP Protocol", "imap"),
            new LabelValueBean("POP3 Protocol", "pop3")
        );
    }

    /**
     * Cleanup method called when the application shuts down.
     * Saves and closes the database.
     */
    @PreDestroy
    public void cleanup() {
        log.info("Finalizing memory database");
        if (database != null) {
            try {
                database.close();
            } catch (Exception e) {
                log.error("Error closing memory database: {}", e.getMessage());
            }
        }
    }

    /**
     * Simple bean to hold label-value pairs for dropdown options.
     * This replaces the Struts LabelValueBean.
     */
    public static class LabelValueBean {
        private final String label;
        private final String value;

        public LabelValueBean(String label, String value) {
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
