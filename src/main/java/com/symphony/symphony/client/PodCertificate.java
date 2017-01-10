package com.symphony.symphony.client;

import lombok.Data;

/**
 * Represents the public signing certificate used by the Symphony Pod to sign JWTs.  The certificate
 * is returned as PEM string.
 * <p>
 * https://en.wikipedia.org/wiki/Privacy-enhanced_Electronic_Mail
 */
@Data
public class PodCertificate {
    private String certificate;
}
