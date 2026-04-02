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
package org.apache.struts.webapp.example2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Spring MVC controller serving the welcome/index page.
 * Replaces the original {@code index.jsp} and the Tiles
 * {@code .welcome} definition.
 */
@Controller
public class WelcomeController {

    /**
     * Display the welcome page.
     * Also handles the root URL by redirecting to /welcome.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/welcome";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
}
