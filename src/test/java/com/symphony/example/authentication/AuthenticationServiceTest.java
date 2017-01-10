package com.symphony.example.authentication;

import com.symphony.example.utils.SecurityKeyUtils;
import com.symphony.symphony.client.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.security.auth.login.LoginException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * JUnit test for AuthenticationService.
 *
 * Created by Dan Nathanson on 1/2/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
    private final static String TEST_PUBLIC_KEY = "-----BEGIN CERTIFICATE-----\n"
            + "MIIDIDCCAggCAQEwDQYJKoZIhvcNAQELBQAwVjELMAkGA1UEBhMCVVMxJDAiBgNV"
            + "BAoMG1N5bXBob255IENvbW11bmljYXRpb25zIExMQzEhMB8GA1UEAwwYVHJ1c3Rl\n"
            + "ZCBDQSBSb290IGZvciBQb2QgMB4XDTE1MDcxNDAxMzQwMloXDTI1MDQxMjAxMzQw\n"
            + "MlowVjELMAkGA1UEBhMCVVMxJDAiBgNVBAoMG1N5bXBob255IENvbW11bmljYXRp\n"
            + "b25zIExMQzEhMB8GA1UEAwwYVHJ1c3RlZCBDQSBSb290IGZvciBQb2QgMIIBIjAN\n"
            + "BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtqqklfEco8t8Chi41GtsjzP7zG9r\n"
            + "8PEAhoC9PV4pq67A8WrnONzdPEnWzOCjgb5xuTkUWKTUIy6Rh9OauhvK3FdxADqP\n"
            + "U6z/zutueH40HvA64FAWRxSHfuBvhnk/8fSQqokxf/x1ydnHiZd9aVCUtv5sqiVN\n"
            + "LadOq98zRhfAbreDR+LsZfniEfhRZbGAR8muHdInAAjfUicbW0f0C09lMlZP9IAo\n"
            + "2CD6HwEJzMs6iR4yb28Wl/9H1P9/kdGU27Q6wjYcUZ4S1y53L0ZdLZkYVCNd3AOi\n"
            + "EFjd3wgBIG77w4+woTb5cxUy+3pLDWCSo03HTgKxwfR2DTXyYqrj0jGnUQIDAQAB\n"
            + "MA0GCSqGSIb3DQEBCwUAA4IBAQA+7w9xbQUYGGRjy8WKOfIY9N3jOl/AHZ1Rw9Wr\n"
            + "iTjVTwyOX/qv7m6TGwK2/ERVSnQSY/wuwGrWXPmQbFZ49LmyA1PBQa0kXy/VmocL\n"
            + "SOskPEyaGt9RlYDCgX0jSl3iC4k7SLsB5uNxZ8xOgiL16u/J2//10abC/j6+poa5\n"
            + "bhQ/Ydt7dKWhcM9TL7TkI0Mv/NTU5yL5RXg1Ldnk6tMZMM8lGF5SDFq/oQOLlQu5\n"
            + "JjxAveXsvq0jbpjmYKg6WD8Oo6W02SZMiJtTNQrzXArMWQ22sVlnyGF/BjJOE0dR\n"
            + "D+VyIqfUuKvYEJn3WHVSbqJ5sC6e8zADTl6si0TYl3JOeDBO\n"
            + "-----END CERTIFICATE-----\n";

    private final static String TEST_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n"
            +"MIIEpQIBAAKCAQEAtqqklfEco8t8Chi41GtsjzP7zG9r8PEAhoC9PV4pq67A8Wrn\n"
            +"ONzdPEnWzOCjgb5xuTkUWKTUIy6Rh9OauhvK3FdxADqPU6z/zutueH40HvA64FAW\n"
            +"RxSHfuBvhnk/8fSQqokxf/x1ydnHiZd9aVCUtv5sqiVNLadOq98zRhfAbreDR+Ls\n"
            +"ZfniEfhRZbGAR8muHdInAAjfUicbW0f0C09lMlZP9IAo2CD6HwEJzMs6iR4yb28W\n"
            +"l/9H1P9/kdGU27Q6wjYcUZ4S1y53L0ZdLZkYVCNd3AOiEFjd3wgBIG77w4+woTb5\n"
            +"cxUy+3pLDWCSo03HTgKxwfR2DTXyYqrj0jGnUQIDAQABAoIBABAYo14vG/X3BVwi\n"
            +"Z48YVD2eOA8AtoNJyWEQ0RwqQwPE522enPduxvP8EbiGBwJ01LKcrAVPqP3cqahN\n"
            +"ZjIGJu8ZqXR6tIyMDxKdvmNRw6gCRT3k8MGctn/gqRMxdggNI/5TDpCXn42E6kde\n"
            +"mQeBuUkSzGKF3PIr7wvIJE2F5EgdL5BvPBatvloge4jaREjtTbcfF4HgtBpDerjK\n"
            +"TDzOF+p+Rk9X6rzSkJHvhxolfMYMKO2Oy/nAr9cV0w9asRl5vgwywbbV6trKTruq\n"
            +"03TgLBMzeawIg2LY9ZvcRDMNsFTrVjCp0DxoQPAT2EHlw374HOCjvo6mTFaBdrXU\n"
            +"DdNu1d0CgYEA2lM965zY3zR7G62fVRheG1abhdqAx4DYxM+GxI2kM8BOmEs+AMJ0\n"
            +"n//1zNR4a04MY/z1uFMMyLaiqWNWzLBvuWMLw6GmRQ8gQRCAnCN3I+kbZhsdtrRf\n"
            +"asmA5bUikSvhq2mKkngIZps8MmQzVz/SggCFs+0TUbalizqjhofKvhcCgYEA1jAk\n"
            +"mmO2LUs18Z1kyrd78lUp1bjfM02UKCpzm8g3JvUw875wCJQKtgmVlloK9Ko/6OE4\n"
            +"W7fc7Sc+SZs6y4fVZMg73DrHRv1roj1hCHeCPVKgMv+0QJaEiKLPUP+sMbxaxAJv\n"
            +"JOBXnEOo9W5DM6SS56V/gnJMVHz2N5QRqgCnTtcCgYEAzW7UbfaJbwWv7Wxi56xl\n"
            +"KK5G6x2eXZtU3WQ0+JAa+QM0MUNjlU1kdgVgYZtvr/Ch4hftksK63cZUqrSpwTMn\n"
            +"/bZAJJXfoU3JWM/RZJpOigDXYi2TuQ5iVlZDLmJRo246C5Pk60t9BCd/7h6w3KFW\n"
            +"UDNzm8kxi4sknCzSIE14LYMCgYEAlkTCeVq5Zj+dAf1VyG9AUzzyk9IXLewgpKlb\n"
            +"4JBwOTHOfUM/YJVUinBg9VII26xSGDVij96H7g4ZQhTZBBAxY3qjnJfUVXbYO8Pq\n"
            +"PsFxq0o7wGvrb2DqbN4kqwQlL+OgnmHCzlFixewmLsKD/UhaFJDky4UbDi95onaM\n"
            +"igPZXZMCgYEA1o//4IqyvnBqItIVis7BG01LqAYo81yBwGSRimQjUWIvSQou0RE9\n"
            +"OVmmnd7Ky0YRxRPWdnadokwdBv2HfM03Qeq1mIPR2huGpHFRIusOLhZKz3XtZw82\n"
            +"8lDA0ieE0BLHShQDunwC6t5aF+6bGasdaNO0zwUXgTzSrQsPOYMoZn8=\n"
            +"-----END RSA PRIVATE KEY-----\n";

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private SymphonyClientFactory symphonyClientFactory;

    @Mock
    private AuthenticationClient mockClient;

    @Mock
    private TokenGenerator tokenGenerator;

    @Test
    public void initiateAppAuthentication() throws Exception {
        when(tokenGenerator.generateToken()).thenReturn("app-token");

        AuthenticateResponse authenticateResponse = new AuthenticateResponse();
        authenticateResponse.setAppToken("app-token");
        authenticateResponse.setSymphonyToken("symphony-token");
        when(mockClient.authenticate(any(AuthenticateRequest.class))).thenReturn(authenticateResponse);

        when(symphonyClientFactory.getAuthenticationClient(eq("test-pod-id"))).thenReturn(mockClient);

        String symphonyToken = authenticationService.initiateAppAuthentication("test-pod-id");

        assertThat(symphonyToken).as("symphony token").isEqualTo("symphony-token");
        assertThat(authenticationService.tokenCache.getIfPresent("app-token")).isEqualTo("symphony-token");
    }

    @Test
    public void validateTokens() throws Exception {
        assertThat(authenticationService.validateTokens(null, null)).isFalse();
        assertThat(authenticationService.validateTokens("app-token", null)).isFalse();
        assertThat(authenticationService.validateTokens(null, "symphony-token")).isFalse();
        assertThat(authenticationService.validateTokens("app-token", "symphony-token")).isFalse();

        authenticationService.tokenCache.put("app-token", "symphony-token");

        assertThat(authenticationService.validateTokens("app-token", "not-symphony-token")).isFalse();
        assertThat(authenticationService.validateTokens("not-app-token", "symphony-token")).isFalse();
        assertThat(authenticationService.validateTokens("app-token", "symphony-token")).isTrue();
    }

    @Test
    public void getUserFromJwt() throws Exception {
        PodCertificate podCertificate = new PodCertificate();
        podCertificate.setCertificate(TEST_PUBLIC_KEY);
        when(mockClient.getPodCertificate()).thenReturn(podCertificate);
        when(symphonyClientFactory.getAuthenticationClient(eq("pod-id"))).thenReturn(mockClient);

        String jwt = generateJwt();
        String displayName = authenticationService.getUserFromJwt(jwt, "pod-id");
        assertThat(displayName).as("Display name").isEqualTo("John Smith");
    }

    @Test
    public void getUserJwtBadCert() throws Exception {
        PodCertificate podCertificate = new PodCertificate();
        podCertificate.setCertificate("bad");
        when(mockClient.getPodCertificate()).thenReturn(podCertificate);
        when(symphonyClientFactory.getAuthenticationClient(eq("pod-id"))).thenReturn(mockClient);

        String jwt = generateJwt();

        assertThatThrownBy(() -> authenticationService.getUserFromJwt(jwt, "pod-id"))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("Couldn't parse cert");
    }

    @Test
    public void getUserJwtUnsigned() throws Exception {
        PodCertificate podCertificate = new PodCertificate();
        podCertificate.setCertificate("bad");
        when(mockClient.getPodCertificate()).thenReturn(podCertificate);
        when(symphonyClientFactory.getAuthenticationClient(eq("pod-id"))).thenReturn(mockClient);

        String jwt = generateUnsignedJwt();

        assertThatThrownBy(() -> authenticationService.getUserFromJwt(jwt, "pod-id"))
                .isInstanceOf(LoginException.class)
                .hasMessageContaining("JWT is not signed");
    }

    private String generateJwt() throws GeneralSecurityException {
        PrivateKey privateKey = SecurityKeyUtils.parseRSAPrivateKey(TEST_PRIVATE_KEY);
        Map<String, String> userInfo = buildUserInfo();
        return Jwts.builder()
                .setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE)
                .claim(Claims.ISSUER, "Symphony Communication Services LLC.")
                .claim(Claims.SUBJECT, "symphony-user-id")
                .claim(Claims.AUDIENCE, "app-id")
                .claim("user", userInfo)
                .claim(Claims.EXPIRATION, System.currentTimeMillis() + 10000)
                .signWith(SignatureAlgorithm.RS512, privateKey)
                .compact();
    }

    private String generateUnsignedJwt() throws GeneralSecurityException {
        Map<String, String> userInfo = buildUserInfo();
        return Jwts.builder()
                .setHeaderParam(JwsHeader.TYPE, JwsHeader.JWT_TYPE)
                .claim(Claims.ISSUER, "Symphony Communication Services LLC.")
                .claim(Claims.SUBJECT, "symphony-user-id")
                .claim(Claims.AUDIENCE, "app-id")
                .claim("user", userInfo)
                .claim(Claims.EXPIRATION, System.currentTimeMillis() + 10000)
                .compact();
    }

    private Map<String, String> buildUserInfo() {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", "symphony-user-id");
        userInfo.put("username", "symphony-username");
        userInfo.put("firstName", "John");
        userInfo.put("lastName", "Smith");
        userInfo.put("displayName", "John Smith");
        userInfo.put("emailAddress", "john.smith@somecompany.com");
        return userInfo;
    }


}