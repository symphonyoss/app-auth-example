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
