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

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about Symphony pods.  PodInfo is stored and retrieved using Pod IDs.  This information comes from
 * Symphony via webhook which is registered when the App is enabled for a pod using the Symphony Admin Portal.
 *
 * This implementation just stores pod info in an in-memory map.  A real implementation would probably store this in
 * something more persistent (ie. a database of some sort).
 */
public class PodDirectory {
    private Map<String, PodInfo> podCache = new HashMap<>();

    /**
     * Adds pod info to the directory.
     */
    public void addPodInfo(PodInfo podInfo) {
        podCache.put(podInfo.getPodId(), podInfo);
    }

    /**
     * Returns pod info for pod with specified ID.  It is an error if there is no pod info.
     *
     * @throws IllegalStateException if no pod info has been registered for the supplied ID
     */
    public PodInfo getPodInfo(String podId) {
        PodInfo podInfo = podCache.get(podId);
        if (podInfo == null) {
            throw new IllegalStateException("Missing pod info for pod with ID '" + podId + "'. " +
                    "This information should have been populated via webhook callback from Symphony when the app was enable for the pod.");
        }
        return podInfo;
    }
}
