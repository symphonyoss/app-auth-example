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

import com.symphony.example.users.User;
import com.symphony.example.users.UserNotFoundException;
import com.symphony.example.users.UserService;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for AuthenticationController using RestAssured and Spring MockMvc.
 *
 * @author Dan Nathanson
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private UserService userService;

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
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .body("pod-id")
        .when()
                .post("/authenticate")
        .then()
                .assertThat(status().isOk())
                .body(equalTo("\"symphony-token\""));
    }

    @Test
    public void validateTokensValid() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(true);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{" +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"appToken\" : \"app-token\"," +
                      "    \"symphonyToken\" : \"symphony-token\"" +
                      "}")
        .when()
                .post("/validate-tokens")
        .then()
                .assertThat(status().isOk())
                .body(equalTo("\"Valid\""));
    }

    @Test
    public void validateTokensInvalid() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(false);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{" +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"appToken\" : \"app-token\"," +
                      "    \"symphonyToken\" : \"symphony-token\"" +
                      "}")
        .when()
                .post("/validate-tokens")
        .then()
                .assertThat(status().isUnauthorized())
                .body(equalTo("\"Invalid\""));
    }

    @Test
    public void validateTokensBadRequest() throws Exception {
        Mockito.when(authenticationService.validateTokens("app-token", "symphony-token")).thenReturn(false);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
        .when()
                .post("/validate-tokens")
        .then()
                .assertThat(status().isBadRequest());
    }


    @Test
    public void jwtLoginMappingFound() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenReturn("symphony-username");
        User user = new User("display-name", "app-username");
        Mockito.when(userService.findBySymphonyId("symphony-username")).thenReturn(user);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login-with-jwt")
        .then()
                .assertThat(status().isOk())
                .body("jwtValid", equalTo(true),
                      "userFound", equalTo(true),
                      "message", equalTo("Hello display-name"),
                      "userDisplayName", equalTo("display-name"));
    }

    @Test
    public void jwtLoginNoMappingFound() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenReturn("symphony-username");
        Mockito.when(userService.findBySymphonyId("symphony-username")).thenReturn(null);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login-with-jwt")
        .then()
                .assertThat(status().isOk())
                .body("jwtValid", equalTo(true),
                      "userFound", equalTo(false),
                      "message", equalTo("Could not find user corresponding to Symphony username from JWT"),
                      "userDisplayName", isEmptyOrNullString());
    }

    @Test
    public void jwtLoginFailedAuth() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id"))
               .thenThrow(new LoginException("expected"));

        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login-with-jwt")
        .then()
                .assertThat(status().isUnauthorized())
                .body("jwtValid", equalTo(false),
                      "userFound", equalTo(false),
                      "message", equalTo("Could not parse or verify signature of JWT"),
                      "userDisplayName", isEmptyOrNullString());
    }

    @Test
    public void jwtLoginBadRequest() throws Exception {
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
        .when()
                .post("/login-with-jwt")
        .then()
                .assertThat(status().isBadRequest())
                .body("jwtValid", equalTo(false),
                      "userFound", equalTo(false),
                      "message", equalTo("Missing JWT or pod ID in request"),
                      "userDisplayName", isEmptyOrNullString());
    }

    @Test
    public void usernameLoginMappingFound() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenReturn("symphony-username");
        User user = new User("display-name", "app-username");
        Mockito.when(userService.get("app-username")).thenReturn(user);
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"username\" : \"app-username\"," +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
                .when()
                .post("/login-with-username")
                .then()
                .assertThat(status().isOk())
                .body("jwtValid", equalTo(true),
                      "userFound", equalTo(true),
                      "message", equalTo("Hello display-name"),
                      "userDisplayName", equalTo("display-name"));
        assertThat(user.getSymphonyId()).isEqualTo("symphony-username");
    }

    @Test
    public void usernameLoginBadRequest() throws Exception {
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"jwt\" : \"the-jwt\"," +
                      "    \"podId\" : \"pod-id\"" +
                      "}")
                .when()
                .post("/login-with-username")
                .then()
                .assertThat(status().isBadRequest())
                .body("jwtValid", equalTo(false),
                        "userFound", equalTo(false),
                        "message", equalTo("Missing username, JWT or pod ID in request"),
                        "userDisplayName", isEmptyOrNullString());
    }

    @Test
    public void usernameLoginUserNotFound() throws Exception {
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenReturn("symphony-username");
        Mockito.when(userService.get("app-username")).thenThrow(new UserNotFoundException("expected"));
        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                        "    \"username\" : \"app-username\"," +
                        "    \"podId\" : \"pod-id\"," +
                        "    \"jwt\" : \"the-jwt\"" +
                        "}")
                .when()
                .post("/login-with-username")
                .then()
                .assertThat(status().isUnauthorized())
                .body("jwtValid", equalTo(false),
                      "userFound", equalTo(false),
                      "message", equalTo("Could not find user with username 'app-username'"),
                      "userDisplayName", isEmptyOrNullString());
    }

    @Test
    public void usernameLoginBadJwt() throws Exception {
        User user = new User("display-name", "app-username");
        Mockito.when(userService.get("app-username")).thenReturn(user);
        Mockito.when(authenticationService.getUserFromJwt("the-jwt", "pod-id")).thenThrow(new LoginException("expected"));

        given()
                .standaloneSetup(new AuthenticationController(authenticationService, userService))
                .contentType("application/json")
                .body("{ " +
                      "    \"username\" : \"app-username\"," +
                      "    \"podId\" : \"pod-id\"," +
                      "    \"jwt\" : \"the-jwt\"" +
                      "}")
                .when()
                .post("/login-with-jwt")
                .then()
                .assertThat(status().isUnauthorized())
                .body("jwtValid", equalTo(false),
                      "userFound", equalTo(false),
                      "message", equalTo("Could not parse or verify signature of JWT"),
                      "userDisplayName", isEmptyOrNullString());
    }


}