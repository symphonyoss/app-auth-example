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

package com.symphony.example.pods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to handle webhook callback from Symphony pod when app is enabled via Symphony Admin Portal.  The
 * callback includes information about the pod on which the app was installed.  It is stored in the PodDirectory.
 *
 * URL for this endpoint should be part of the app manifest.
 *
 * This endpoint is protected by an "API key" to prevent unauthorized access which could inject bogus pod information
 * into the PodDirectory. For this implementation, the API key is just a hardcoded constant.  Real implementations
 * probably want something more elaborate, maybe a different API key for each pod.
 *
 * @author Dan Nathanson
 */
@RestController
public class DirectoryWebhookController {
    private final PodDirectory podDirectory;

    private WebhookConfiguration webhookConfiguration;


    @Autowired
    public DirectoryWebhookController(PodDirectory podDirectory, WebhookConfiguration webhookConfiguration) {
        this.podDirectory = podDirectory;
        this.webhookConfiguration = webhookConfiguration;
    }


    /**
     * Create / update pod info.  This endpoint is protected by an "API key" to prevent unauthorized access which
     * could inject bogus pod information into the PodDirectory. For this implementation, the API key is just a
     * hardcoded constant.  Real implementations probably want something more secure - at least a different API key
     * for each pod. The API key is passed as an HTTP header value with name "API-Key".
     *
     * @param podInfo PodInfo object containing metadata about a pod
     * @param apiKey API Key.  Comes from HTTP Header value "API-Key"
     *
     * @return HTTP 401 - if API key missing or invalid<br/>
     *         HTTP 400 - if PodInfo bad or missing<br/>
     *         HTTP 200 - otherwise.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/podInfo")
    public ResponseEntity updatePodInfo(@RequestBody PodInfo podInfo, @RequestHeader("API-Key") String apiKey) {

        // Check API key
        if (StringUtils.isEmpty(apiKey) || !webhookConfiguration.getApiKey().equals(apiKey)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        // Ensure pod info present and at least has pod ID.
        if (StringUtils.isEmpty(podInfo.getPodId())) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        // Store pod info
        podDirectory.addPodInfo(podInfo);

        return new ResponseEntity(HttpStatus.OK);

    }
}
