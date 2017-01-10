package com.symphony.example.authentication;

import lombok.Data;

/**
 * Token pair.
 *
 * Created by Dan Nathanson on 1/1/17.
 */
@Data
public class ValidateTokensRequest {
    private String podId;
    private String symphonyToken;
    private String appToken;
}
