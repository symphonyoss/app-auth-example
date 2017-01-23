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

package com.symphony.symphony.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the example application.  Properties are read from application.yaml and are prefixed
 * with "example".
 *
 * Created by Dan Nathanson on 12/19/16.
 */
@Data
@ConfigurationProperties(prefix = "symphony.client")
public class SymphonyClientConfiguration {
    private String keystoreFilename;
    private String keystorePassword;
    private String truststoreFilename;
}
