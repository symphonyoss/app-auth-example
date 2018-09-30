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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * App configuration.  Used to configure some variable parts of the App (like app ID).  Setting in Java and then
 * generating a javascript file that is loaded from app.js and component.js.  This allows configuration of this
 * example application to be done in one place (application.yaml).
 *
 * @author Dan Nathanson
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
public class AppConfig {
    private String appId;
}
