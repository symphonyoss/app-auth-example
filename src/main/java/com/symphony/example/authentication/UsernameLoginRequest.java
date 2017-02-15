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

package com.symphony.example.authentication;

import lombok.Data;

/**
 * Request message from authenticating a user with a JSON Web Token. Include the JWT and the ID of the pod that
 * generated the request.
 *
 * @author Dan Nathanson
 */
@Data
public class UsernameLoginRequest {
    private String username;
    private String jwt;
    private String companyId;
}
