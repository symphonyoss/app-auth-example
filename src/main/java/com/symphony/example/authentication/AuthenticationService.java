/*
 * Copyright 2016-2017 Symphony Application Authentication - Symphony LLC
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.symphony.example.authentication;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.symphony.example.utils.SecurityKeyUtils;
import com.symphony.symphony.client.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Service which handles App Authentication flow between Symphony and integrated app.  Flow is initiated by exchanging
 * two tokens between back ends over TLS with bilateral certificate authentication.
 *
 * @author Dan Nathanson
 */
@Component
@Slf4j
public class AuthenticationService {

    // Token cache - maps app token to symphony token.  In this implementation, pod ID is ignored since the assumption
    // is that both tokens are random unique strings.  Also, this won't work for horizontally scaled servers without
    // sticky sessions since it is a local cache
    @VisibleForTesting
    Cache<String, String> tokenCache;

    // Factory / manager for REST clients that talk to Symphony pods over TLS with client certs
    private SymphonyClientFactory symphonyClientFactory;

    private TokenGenerator tokenGenerator;

    @Autowired
    public AuthenticationService(SymphonyClientFactory symphonyClientFactory, TokenGenerator tokenGenerator) {
        this.symphonyClientFactory = symphonyClientFactory;
        this.tokenGenerator = tokenGenerator;

        // Tokens are short lived.  Max is just protection from DDoS attacks
        tokenCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Using a TLS connection with client certificate verification, initiate the App Authentication flow.  This
     * involves creating and sending a unique token to Symphony pod and receiving back another unique token.  These
     * tokens form a pair that will be verified later in the flow via receiving that same symphony token via a
     * different channel (from the web app).  Pod ID comes from the Symphony Client (Front End).
     *
     * @param podId ID of Pod, supplied from Symphony Front End
     * @return token app token
     */
    public String initiateAppAuthentication(String podId) {
        AuthenticationClient authenticationClient = symphonyClientFactory.getAuthenticationClient(podId);

        String appToken = tokenGenerator.generateToken();
        AuthenticateRequest authenticateRequest = new AuthenticateRequest();
        authenticateRequest.setAppToken(appToken);
        AuthenticateResponse authenticateResponse = authenticationClient.authenticate(authenticateRequest);
        String syphonyToken = authenticateResponse.getSymphonyToken();
        log.info("App Token: {}, Symphony Token: {}]", appToken, syphonyToken);
        tokenCache.put(appToken, syphonyToken);
        return appToken;
    }

    /**
     * Validates token pairs.  Returns true iff both tokens present and exist in the token cache.
     *
     * @param appToken application token
     * @param symphonyToken symphony token
     * @return true if tokens validate, false otherwise
     */
    public boolean validateTokens(String appToken, String symphonyToken) {
        if (appToken != null && symphonyToken != null) {
            String savedSymphonyToken = tokenCache.getIfPresent(appToken);
            return symphonyToken.equals(savedSymphonyToken);
        }
        return false;
    }

    /**
     * Validates JWT received from Javascript front end (via Symphony) by checking signature against public signing
     * cert retrieved from Symphony pod.  If JWT is invalid, throw LoginException otherwise return user name from
     * JWT.  A real implementation of this would use user ID from the JWT to look up a User object from a user store
     * and return that User object.
     *
     * This method gets the signing cert for the pod for every request.  A better implementation would be to retrieve
     * it once and cache it.
     *
     * The format of the JWT claims is:
     * {@code
     * {
     *     "aud" : "<id of app>",
     *     "iss" : "Symphony Communication Services LLC.",
     *     "sub" : "<Symphony user ID>",
     *     "exp" : "<expiration date in millis>",
     *     "user" : {
     *         "id" : "<Symphony user ID>",
     *         "emailAddress" : "<email address>",
     *         "username" : "<Symphony username>",
     *         "firstName" : "<first name>",
     *         "lastName" : "<last name>",
     *         "displayName" : "<display name>",
     *         "title" : "<title>",
     *         "company" : "<company>",
     *         "companyId" : "<company (pod) ID>",
     *         "location" : "<location>",
     *         "avatarUrl" : "<URL for user's avatar>",
     *         "avatarSmallUrl" : "<URL for user's small avatar>"
     *     }
     * }
     * }
     *
     * @param jwt JSON Web Token from Symphony which includes user identification information
     * @param podId ID of pod which generated the JWT
     * @return user's symphony username
     * @throws LoginException if JWT is invalid
     */
    public String getUserFromJwt(String jwt, String podId) throws LoginException {

        AuthenticationClient authenticationClient = symphonyClientFactory.getAuthenticationClient(podId);

        // Don't trust a JWT that isn't signed!!!
        if (!Jwts.parser().isSigned(jwt)) {
            throw new LoginException("JWT is not signed");
        }

        // Get the cert from the pod.  Should probably cache this.
        PodCertificate podCertificate = authenticationClient.getPodCertificate();

        // Get the public key from the cert
        PublicKey publicKey;
        try {
            X509Certificate x509Certificate = SecurityKeyUtils.parseX509Certificate(podCertificate.getCertificate());
            publicKey = x509Certificate.getPublicKey();
        } catch (GeneralSecurityException e) {
            throw new LoginException("Couldn't parse cert string from Symphony into X509Certificate object:" + e.getMessage());
        }

        DefaultClaims claims = (DefaultClaims) Jwts.parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(jwt)
                .getBody();

        Map userMap = (Map) claims.get("user");
        return (String) userMap.get("username");
    }
}
