package com.symphony.example.authentication;

import lombok.Data;

/**
 * Request message from authenticating a user with a JSON Web Token. Include the JWT and the ID of the pod that
 * generated the request.
 *
 * Created by Dan Nathanson on 1/1/17.
 */
@Data
public class JwtLoginRequest {
    private String jwt;
    private String podId;
}
