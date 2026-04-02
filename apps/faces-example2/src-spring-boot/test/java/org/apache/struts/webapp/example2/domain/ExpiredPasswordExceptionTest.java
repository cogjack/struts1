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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpiredPasswordExceptionTest {

    @Test
    void constructor_ShouldSetUsername() {
        ExpiredPasswordException ex = new ExpiredPasswordException("testuser");
        assertThat(ex.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getMessage_ShouldContainUsername() {
        ExpiredPasswordException ex = new ExpiredPasswordException("admin");
        assertThat(ex.getMessage()).contains("admin");
    }

    @Test
    void exception_ShouldBeRuntimeException() {
        ExpiredPasswordException ex = new ExpiredPasswordException("testuser");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void exception_ShouldNotBeStrutsModuleException() {
        // Verify the Spring-friendly exception does not depend on Struts
        ExpiredPasswordException ex = new ExpiredPasswordException("testuser");
        assertThat(ex.getClass().getSuperclass()).isEqualTo(RuntimeException.class);
    }

    @Test
    void exception_ShouldBeThrowable() {
        assertThatThrownBy(() -> {
            throw new ExpiredPasswordException("expired_user");
        })
            .isInstanceOf(ExpiredPasswordException.class)
            .hasMessageContaining("expired_user");
    }
}
