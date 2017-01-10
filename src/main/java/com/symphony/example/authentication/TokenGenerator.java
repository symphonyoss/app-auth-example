package com.symphony.example.authentication;

import java.util.UUID;

/**
 * Token generator for app tokens.  This is a simple implementation that just creates a random string.
 *
 * Created by Dan Nathanson on 1/2/17.
 */
public class TokenGenerator {

    /**
     * Generates token using UUID random generator.
     */
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
