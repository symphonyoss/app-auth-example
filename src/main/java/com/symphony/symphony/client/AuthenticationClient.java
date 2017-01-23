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

import feign.Headers;
import feign.RequestLine;

/**
 * Client API for Symphony Back End authentication endpoints. These endpoints may required client
 * certificates for TLS authentication.
 */
public interface AuthenticationClient {

    @RequestLine("GET /sessionauth/v1/app/pod/certificate")
    @Headers("Content-Type: application/json")
    PodCertificate getPodCertificate();

    @RequestLine("POST /sessionauth/v1/authenticate/extensionApp")
    @Headers("Content-Type: application/json")
    AuthenticateResponse authenticate(AuthenticateRequest request);
}
