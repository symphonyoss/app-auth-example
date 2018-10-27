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

import lombok.Data;

/**
 * Webhook payload from Symphony containing information about the pod.
 */
@Data
public class PodInfo {
    private String appId;
    private String companyId;
    private EventType eventType;
    private PodInfoPayload payload;

    public enum EventType {
        APP_ENABLED,
        appEnabled,
        AGENT_REGISTERED,
        agentRegistered;
    }

    @Data
    public static class PodInfoPayload {
        private String agentUrl;
        private String podUrl;
        private String sessionAuthUrl;
    }
}
