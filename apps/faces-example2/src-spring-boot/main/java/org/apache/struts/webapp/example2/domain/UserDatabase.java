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

/**
 * A Data Access Object (DAO) interface describing the available operations
 * for retrieving and storing Users (and their associated Subscriptions) in
 * some persistence layer whose characteristics are not specified here.
 * One or more implementations will be created to perform the actual I/O that is required.
 */
public interface UserDatabase {

    /**
     * Create and return a new User defined in this user database.
     *
     * @param username Username of the new user
     * @return The newly created user
     * @throws IllegalArgumentException if the specified username is not unique
     */
    User createUser(String username);

    /**
     * Finalize access to the underlying persistence layer.
     *
     * @throws Exception if a database access error occurs
     */
    void close() throws Exception;

    /**
     * Return the existing User with the specified username, if any;
     * otherwise return null.
     *
     * @param username Username of the user to retrieve
     * @return The user with the specified username, or null if not found
     */
    User findUser(String username);

    /**
     * Return the set of Users defined in this user database.
     *
     * @return An array of all users in the database
     */
    User[] findUsers();

    /**
     * Initiate access to the underlying persistence layer.
     *
     * @throws Exception if a database access error occurs
     */
    void open() throws Exception;

    /**
     * Remove the specified User from this database.
     *
     * @param user User to be removed
     * @throws IllegalArgumentException if the specified user is not associated with this database
     */
    void removeUser(User user);

    /**
     * Save any pending changes to the underlying persistence layer.
     *
     * @throws Exception if a database access error occurs
     */
    void save() throws Exception;
}
