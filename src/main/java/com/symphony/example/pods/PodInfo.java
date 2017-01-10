package com.symphony.example.pods;

import lombok.Data;

/**
 * Stores pod host information.
 */
@Data
public class PodInfo {
    private String podId;
    private String agentHost;
    private String podHost;
}
