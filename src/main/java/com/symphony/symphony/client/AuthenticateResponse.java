package com.symphony.symphony.client;

import lombok.Data;

import java.io.Serializable;

/**
 * AuthenticateResponse - This is a model class that contain both the Application Token and Symphony Token for the
 * Extension API to validate the Client Application
 */

@Data
public class AuthenticateResponse implements Serializable {

    /**
     * Unique identifier of the app
     */
    private String appId;
    private String appToken;
    private String symphonyToken;
    private Long expireAt;

}

