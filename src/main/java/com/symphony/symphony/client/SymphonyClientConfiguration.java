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
