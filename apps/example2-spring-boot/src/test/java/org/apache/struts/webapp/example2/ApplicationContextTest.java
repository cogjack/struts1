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
import org.apache.struts.webapp.example2.domain.UserDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestDatabaseConfiguration.class)
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserDatabase userDatabase;

    @Autowired
    private List<DatabaseConfiguration.LabelValueBean> serverTypes;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void applicationBeanIsLoaded() {
        assertThat(context.getBean(Application.class)).isNotNull();
    }

    @Test
    void databaseConfigurationIsLoaded() {
        assertThat(context.getBean(DatabaseConfiguration.class)).isNotNull();
    }

    @Test
    void userDatabaseBeanIsLoaded() {
        assertThat(userDatabase).isNotNull();
    }

    @Test
    void serverTypesBeanIsLoaded() {
        assertThat(serverTypes).isNotNull();
        assertThat(serverTypes).hasSize(2);
    }

    @Test
    void serverTypesContainsImapAndPop3() {
        assertThat(serverTypes)
            .extracting(DatabaseConfiguration.LabelValueBean::getValue)
            .containsExactlyInAnyOrder("imap", "pop3");
    }
}
