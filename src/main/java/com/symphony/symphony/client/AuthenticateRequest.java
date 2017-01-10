package com.symphony.symphony.client;

import lombok.Data;


/**
 * Request for Application Authentication against Symphony pod.  The appToken should be a random
 * string. It has no meaning to Symphony.
 */
@Data
public class AuthenticateRequest {

    // Application token.  Can be any unique, random-ish string.
    private String appToken;
}
