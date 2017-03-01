/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.symphony.example.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Controller that makes some configuration from application.yaml available in the javascript apps.  Does this by
 * dynamically generating a javascript file that sets some global variables.  The generated file is included as
 * a script from the HTML files.
 *
 * @author Dan Nathanson
 */
@RestController
public class AppConfigController {
    private final AppConfig appConfig;

    @Autowired
    public AppConfigController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    /**
     * Generate a javascript file that sets some global variables, like appId
     */
    @RequestMapping(value = "/config/appConfig", method = RequestMethod.GET, produces = "text/javascript")
    public void appConfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/javascript");
        PrintWriter writer = response.getWriter();
        writer.write("var appId = '" + appConfig.getAppId() + "';\n");
        writer.flush();
        writer.close();
    }
}
