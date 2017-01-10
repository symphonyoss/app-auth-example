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
