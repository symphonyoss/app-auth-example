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
import com.symphony.example.web.AppConfig;
import com.symphony.symphony.client.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;
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

    private AuthenticationConfig authenticationConfig;

    private AppConfig appConfig;

    @Autowired
    public AuthenticationService(SymphonyClientFactory symphonyClientFactory, TokenGenerator tokenGenerator, AppConfig appConfig, AuthenticationConfig authenticationConfig) {
        this.symphonyClientFactory = symphonyClientFactory;
        this.tokenGenerator = tokenGenerator;
        this.appConfig = appConfig;
        this.authenticationConfig = authenticationConfig;

        // Tokens are short lived.  Max is just protection from DDoS attacks
        tokenCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Using a signed JWT for authentication, initiate the App Authentication flow.  This involves creating and sending
     * a unique token to Symphony pod and receiving back another unique token.  These tokens form a pair that will be
     * verified later in the flow via receiving that same symphony token via a different channel (from the web app).
     * Pod ID comes from the Symphony Client (Front End).  The JWT includes this app's ID and is signed using this app's
     * private key.  The public key is part of this app's meta data which is included when the app is installed on a
     * pod.
     *
     * @param companyId ID of company/pod, supplied from Symphony Front End
     * @return token app token
     */

    public String initiateAppAuthentication(String companyId) {
        String appToken = tokenGenerator.generateToken();
        log.info("App Token: {}", appToken);

        String privateKeyPem = SecurityKeyUtils.readPemFromFile(authenticationConfig.getPrivateKeyPemFilename());
        PrivateKey privateKey;
        try {
            privateKey = SecurityKeyUtils.parseRSAPrivateKey(privateKeyPem);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error parsing private key PEM file", e);
        }

        String authToken = createSignedAuthenticationJwt(appConfig.getAppId(), privateKey);
        log.info("App authentication JWT: {}", authToken);

        AuthenticationClient authenticationClient = symphonyClientFactory.getAuthenticationClient(companyId);


        AuthenticateRequest authenticationRequest = new AuthenticateRequest(appToken, authToken);
        AuthenticateResponse authenticateResponse = authenticationClient.authenticate(authenticationRequest);
        String symphonyToken = authenticateResponse.getSymphonyToken();

        log.info("App Token: {}, Symphony Token: {}]", appToken, symphonyToken);
        tokenCache.put(appToken, symphonyToken);

        InitiateAuthResponse response = new InitiateAuthResponse(authToken, appToken);

        return appToken;
    }

    /**
     * Creates a JWT with the provided app ID, signed with the provided private key.  Expiration date is set for
     * 5 seconds in the future.
     *
     * @param appId the app ID to initiateAppAuth; will be verified by the pod
     * @param privateKey the private RSA key to be used to sign the authentication request; will be checked on the pod against
     * the public key stored for the app
     */
    private String createSignedAuthenticationJwt(String appId, Key privateKey) {

        return Jwts.builder()
                   .setSubject(appId)
                   .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)))
                   .signWith(SignatureAlgorithm.RS512, privateKey)
                   .compact();
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
     * @param companyId company ID for pod which generated the JWT
     * @return user's symphony username
     * @throws LoginException if JWT is invalid
     */
    public String getUserFromJwt(String jwt, String companyId) throws LoginException {

        AuthenticationClient authenticationClient = symphonyClientFactory.getAuthenticationClient(companyId);

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

        Jws<Claims> claims = Jwts.parser()
                                 .setSigningKey(publicKey)
                                 .parseClaimsJws(jwt);

        // Double check that the algorithm is the expected RS512 to ensure cookie hasn't been forged and signed using
        // public key.
        if (!SignatureAlgorithm.RS512.name().equals(claims.getHeader().getAlgorithm())) {
            throw new LoginException("Invalid JWT algorithm: " + claims.getHeader().getAlgorithm());
        }

        Map userMap = (Map) claims.getBody().get("user");
        return (String) userMap.get("username");
    }
}
