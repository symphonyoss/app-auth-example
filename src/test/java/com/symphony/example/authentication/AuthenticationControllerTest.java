package com.symphony.example.authentication;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.security.auth.login.LoginException;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for AuthenticationController using RestAssured and Spring MockMvc.
 * <p>
 * Created by Dan Nathanson on 1/2/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
        RestAssuredMockMvc.reset();
    }

    @Test
    public void authenticate() throws Exception {
        Mockito.when(authenticationService.initiateAppAuthentication("pod-id")).thenReturn("symphony-token");
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .body("pod-id")
        .when()
                .post("/authenticate")
        .then()
                .assertThat(status().isOk())
                .body(equalTo("symphony-token"));
    }

    @Test
    public void validateTokensValid() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(true);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .contentType("application/json")
                .body("{" +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"appToken\" : \"app-token\"," +
                      "    \"symphonyToken\" : \"symphony-token\"" +
                      "}")
        .when()
                .post("/validateTokens")
        .then()
                .assertThat(status().isOk())
                .body(equalTo("Valid"));
    }

    @Test
    public void validateTokensInvalid() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(false);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .contentType("application/json")
                .body("{" +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"appToken\" : \"app-token\"," +
                      "    \"symphonyToken\" : \"symphony-token\"" +
                      "}")
        .when()
                .post("/validateTokens")
        .then()
                .assertThat(status().isUnauthorized())
                .body(equalTo("Invalid"));
    }

    @Test
    public void validateTokensBadRequest() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(false);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
        .when()
                .post("/validateTokens")
        .then()
                .assertThat(status().isBadRequest());
    }


    @Test
    public void login() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenReturn("display-name");
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .contentType("application/json")
                .body("{ " +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login")
        .then()
                .assertThat(status().isOk())
                .body(equalTo("Hello display-name!"));
    }

    @Test
    public void loginFailedAuth() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id"))
               .thenThrow(new LoginException("expected"));

        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .contentType("application/json")
                .body("{ " +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login")
        .then()
                .assertThat(status().isUnauthorized())
                .body(equalTo("Authentication failed"));
    }

    @Test
    public void loginBadRequest() throws Exception {
        given()
                .standaloneSetup(new AuthenticationController(authenticationService))
                .contentType("application/json")
                .body("{ " +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login")
        .then()
                .assertThat(status().isBadRequest())
                .body(equalTo("Bad request"));
    }
}