package com.symphony.example.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Authentication controller.  These methods are called by the integrated app's front end which is displayed in an
 * iFrame in the Symphony Client.  The endpoints' URLs and implementation details are up to the integration partners
 * discretion.
 *
 * Created by Dan Nathanson on 12/19/16.
 */
@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Start the app authentication flow.  At this point, only the pod ID is known (which was provided to the
     * app by the Symphony Client Extension API).  Pod ID is just a unique string and usually is based on the name of
     * the company who the pod serves.  AuthenticationService will exchange tokens with Symphony back end.  The token
     * from Symphony is returned from this endpoint.
     *
     * @return Symphony token
     */
    @RequestMapping(method = POST, path = "/authenticate")
    public String authenticate(@RequestBody String podId) {
        return authenticationService.initiateAppAuthentication(podId);
    }


    /**
     * Validate token pair.  This implementation assumes statelessness on the server side so both tokens are required
     * in the request.  If this application supported sticky sessions, only the Symphony token would be required
     * since the application token could have been saved in session state.
     *
     * @param request object containing both authentication tokens
     * @return "Valid" (and HTTP 200) or "Invalid" (and HTTP 401)
     */
    @RequestMapping(method = POST, path = "/validateTokens")
    public ResponseEntity<String> validateTokens(@RequestBody ValidateTokensRequest request) {
        if (authenticationService.validateTokens(request.getAppToken(), request.getSymphonyToken())) {
            return new ResponseEntity<>("Valid", HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>("Invalid", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(method = POST, path = "/login")
    public ResponseEntity<String> login(@RequestBody JwtLoginRequest request) {

        if (StringUtils.isEmpty(request.getJwt()) || StringUtils.isEmpty(request.getPodId())) {
            return new ResponseEntity<>("Bad request", HttpStatus.BAD_REQUEST);
        }

        try {
            String user = authenticationService.getUserFromJwt(request.getJwt(), request.getPodId());
            return new ResponseEntity<>("Hello " + user + "!", HttpStatus.OK);
        } catch (LoginException e) {
            return new ResponseEntity<>("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }
}
