package com.symphony.example.pods;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for /podInfo webhook.  Properties are read from application.yaml and are prefixed with "webhook".
 *
 * Created by Dan Nathanson on 12/19/16.
 */
@Data
@ConfigurationProperties(prefix = "webhook")
public class WebhookConfiguration {
    private String apiKey;
}
