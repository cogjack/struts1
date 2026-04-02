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
package org.apache.struts.webapp.example2.memory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.struts.webapp.example2.Subscription;
import org.apache.struts.webapp.example2.User;
import org.apache.struts.webapp.example2.UserDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * <p>Concrete implementation of {@link UserDatabase} for an in-memory
 * database backed by an XML data file.</p>
 *
 * <p>This implementation has been adapted for Spring Boot, removing dependencies
 * on Apache Commons Digester and using standard Java XML parsing instead.</p>
 *
 * @author Craig R. McClanahan
 * @since Struts 1.1
 */
public final class MemoryUserDatabase implements UserDatabase {

    private static final Logger log = LoggerFactory.getLogger(MemoryUserDatabase.class);

    /**
     * The {@link User}s associated with this UserDatabase, keyed by username.
     */
    private final Map<String, User> users = new HashMap<>();

    /**
     * Absolute pathname to the persistent file we use for loading and storing
     * persistent data.
     */
    private String pathname = null;

    private String pathnameOld = null;

    private String pathnameNew = null;

    public String getPathname() {
        return this.pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
        this.pathnameOld = pathname + ".old";
        this.pathnameNew = pathname + ".new";
    }

    @Override
    public void close() throws Exception {
        save();
    }

    @Override
    public User createUser(String username) {
        synchronized (users) {
            if (users.get(username) != null) {
                throw new IllegalArgumentException("Duplicate user '" + username + "'");
            }
            log.trace("Creating user '{}'", username);
            MemoryUser user = new MemoryUser(this, username);
            users.put(username, user);
            return user;
        }
    }

    @Override
    public User findUser(String username) {
        synchronized (users) {
            return users.get(username);
        }
    }

    @Override
    public User[] findUsers() {
        synchronized (users) {
            User[] results = new User[users.size()];
            return users.values().toArray(results);
        }
    }

    /**
     * <p>Initiate access to the underlying persistence layer.</p>
     * Uses standard Java DOM parsing instead of Apache Commons Digester.
     *
     * @throws Exception if a database access error occurs
     */
    @Override
    public void open() throws Exception {
        InputStream is = null;
        BufferedInputStream bis = null;

        try {
            log.debug("Loading database from '{}'", pathname);

            File file = new File(pathname);
            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                is = getClass().getClassLoader().getResourceAsStream(pathname);
                if (is == null) {
                    throw new IOException("Cannot find database file: " + pathname);
                }
            }
            bis = new BufferedInputStream(is);

            parseDatabase(bis);

        } catch (Exception e) {
            log.error("Loading database from '{}': {}", pathname, e.getMessage());
            throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Throwable t) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Load the database from an InputStream. This is useful for loading from classpath resources.
     *
     * @param inputStream The input stream to load from
     * @throws Exception if a database access error occurs
     */
    public void open(InputStream inputStream) throws Exception {
        BufferedInputStream bis = null;

        try {
            log.debug("Loading database from input stream");
            bis = new BufferedInputStream(inputStream);

            parseDatabase(bis);

        } catch (Exception e) {
            log.error("Loading database from input stream: {}", e.getMessage());
            throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Throwable t) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Parse the database XML from the given input stream using DOM.
     */
    private void parseDatabase(InputStream inputStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Prevent XXE attacks
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);

        Element root = document.getDocumentElement();
        NodeList userNodes = root.getElementsByTagName("user");

        for (int i = 0; i < userNodes.getLength(); i++) {
            Element userElement = (Element) userNodes.item(i);
            String username = userElement.getAttribute("username");
            User user = createUser(username);
            user.setFromAddress(getAttributeOrNull(userElement, "fromAddress"));
            user.setFullName(getAttributeOrNull(userElement, "fullName"));
            user.setPassword(getAttributeOrNull(userElement, "password"));
            user.setReplyToAddress(getAttributeOrNull(userElement, "replyToAddress"));

            NodeList subscriptionNodes = userElement.getElementsByTagName("subscription");
            for (int j = 0; j < subscriptionNodes.getLength(); j++) {
                Element subElement = (Element) subscriptionNodes.item(j);
                String host = subElement.getAttribute("host");
                Subscription subscription = user.createSubscription(host);

                String autoConnect = getAttributeOrNull(subElement, "autoConnect");
                if ("true".equalsIgnoreCase(autoConnect) || "yes".equalsIgnoreCase(autoConnect)) {
                    subscription.setAutoConnect(true);
                } else {
                    subscription.setAutoConnect(false);
                }
                subscription.setPassword(getAttributeOrNull(subElement, "password"));
                subscription.setType(getAttributeOrNull(subElement, "type"));
                subscription.setUsername(getAttributeOrNull(subElement, "username"));
            }
        }
    }

    private String getAttributeOrNull(Element element, String attributeName) {
        String value = element.getAttribute(attributeName);
        return value.isEmpty() ? null : value;
    }

    @Override
    public void removeUser(User user) {
        if (this != user.getDatabase()) {
            throw new IllegalArgumentException("User not associated with this database");
        }
        log.trace("Removing user '{}'", user.getUsername());
        synchronized (users) {
            users.remove(user.getUsername());
        }
    }

    @Override
    public void save() throws Exception {
        if (pathname == null) {
            log.debug("No pathname set, skipping save");
            return;
        }

        log.debug("Saving database to '{}'", pathname);
        File fileNew = new File(pathnameNew);
        PrintWriter writer = null;

        try {
            FileOutputStream fos = new FileOutputStream(fileNew);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            writer = new PrintWriter(osw);

            writer.println("<?xml version='1.0'?>");
            writer.println("<database>");

            User[] allUsers = findUsers();
            for (User user : allUsers) {
                writer.print("  ");
                writer.println(user);
                Subscription[] subscriptions = user.getSubscriptions();
                for (Subscription subscription : subscriptions) {
                    writer.print("    ");
                    writer.println(subscription);
                    writer.print("    ");
                    writer.println("</subscription>");
                }
                writer.print("  ");
                writer.println("</user>");
            }

            writer.println("</database>");

            if (writer.checkError()) {
                writer.close();
                fileNew.delete();
                throw new IOException("Saving database to '" + pathname + "'");
            }
            writer.close();
            writer = null;

        } catch (IOException e) {
            if (writer != null) {
                writer.close();
            }
            fileNew.delete();
            throw e;
        }

        File fileOrig = new File(pathname);
        File fileOld = new File(pathnameOld);
        if (fileOrig.exists()) {
            fileOld.delete();
            if (!fileOrig.renameTo(fileOld)) {
                throw new IOException("Renaming '" + pathname + "' to '" + pathnameOld + "'");
            }
        }
        if (!fileNew.renameTo(fileOrig)) {
            if (fileOld.exists()) {
                fileOld.renameTo(fileOrig);
            }
            throw new IOException("Renaming '" + pathnameNew + "' to '" + pathname + "'");
        }
        fileOld.delete();
    }
}
