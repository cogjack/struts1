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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

class ConstantsTest {

    @Test
    void packageConstant_ShouldMatchExpectedValue() {
        assertThat(Constants.PACKAGE).isEqualTo("org.apache.struts.webapp.example2");
    }

    @Test
    void subscriptionKey_ShouldBeSubscription() {
        assertThat(Constants.SUBSCRIPTION_KEY).isEqualTo("subscription");
    }

    @Test
    void userKey_ShouldBeUser() {
        assertThat(Constants.USER_KEY).isEqualTo("user");
    }

    @Test
    void class_ShouldBeFinal() {
        assertThat(Modifier.isFinal(Constants.class.getModifiers())).isTrue();
    }

    @Test
    void constructor_ShouldBePrivate() throws Exception {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    void constants_ShouldNotContainDatabaseKey() throws Exception {
        // Verify that the Struts-specific DATABASE_KEY has been removed
        // during the Spring Boot migration
        java.lang.reflect.Field[] fields = Constants.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            assertThat(field.getName()).isNotEqualTo("DATABASE_KEY");
        }
    }
}
