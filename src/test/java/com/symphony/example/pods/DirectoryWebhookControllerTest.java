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

package com.symphony.example.pods;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for DirectoryWebhookController using RestAssured and Spring MockMvc.
 *
 * @author Dan Nathanson
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class DirectoryWebhookControllerTest {

    @Mock
    private PodDirectory podDirectory;
    private WebhookConfiguration webhookConfiguration;

    @Before
    public void setup() {
        this.webhookConfiguration = new WebhookConfiguration();
        webhookConfiguration.setApiKey("super-secret-api-key-1234");
    }

    @After
    public void tearDown() {
        RestAssuredMockMvc.reset();
    }

    /**
     * Test happy path
     */
    @Test
    public void updatePodInfo() throws Exception {
        PodInfo podInfo = new PodInfo();
        podInfo.setCompanyId("company-id");
        podInfo.setAppId("app-id");
        podInfo.setEventType(PodInfo.EventType.APP_ENABLED);
        PodInfo.PodInfoPayload payload = new PodInfo.PodInfoPayload();
        podInfo.setPayload(payload);
        payload.setAgentUrl("agent-url");
        payload.setPodUrl("pod-url");
        payload.setSessionAuthUrl("sessionauth-url");

        given()
                .standaloneSetup(new DirectoryWebhookController(podDirectory, webhookConfiguration))
                .contentType("application/json")
                .body("{ " +
                      "    \"appId\" : \"app-id\"," +
                      "    \"companyId\" : \"company-id\"," +
                      "    \"eventType\" : \"APP_ENABLED\"," +
                      "    \"payload\" : {" +
                      "        \"podUrl\" : \"pod-url\"," +
                      "        \"agentUrl\" : \"agent-url\"," +
                      "        \"sessionAuthUrl\" : \"sessionauth-url\"" +
                      "     }" +
                      "}")
                .header("api-key", "super-secret-api-key-1234")
        .when()
                .post("/podInfo")
        .then()
                .assertThat(status().isOk());

        Mockito.verify(podDirectory, times(1)).addPodInfo(eq(podInfo));
    }

    /**
     * Test invalid API key.
     */
    @Test
    public void updatePodInfoBadKey() throws Exception {
        PodInfo podInfo = new PodInfo();

        Mockito.when(podDirectory.getPodInfo("pod-id")).thenReturn(podInfo);
        given()
                .standaloneSetup(new DirectoryWebhookController(podDirectory, webhookConfiguration))
                .contentType("application/json")
                .body(  "{ " +
                        "    \"appId\" : \"appId-id\"," +
                        "    \"companyId\" : \"company-id\"," +
                        "    \"eventType\" : \"APP_ENABLED\"," +
                        "    \"payload\" : {" +
                        "        \"podUrl\" : \"pod-url\"," +
                        "        \"agentUrl\" : \"agent-url\"," +
                        "        \"sessionAuthUrl\" : \"sessionauth-url\"" +
                        "     }" +
                        "}")
                .header("api-key", "this-is-not-the-key")
        .when()
                .post("/podInfo")
        .then()
                .assertThat(status().isUnauthorized());

        Mockito.verify(podDirectory, never()).getPodInfo(anyString());
    }

    /**
     * Test bad request (missing companyId)
     */
    @Test
    public void updatePodInfoBadRequest() throws Exception {
        PodInfo podInfo = new PodInfo();

        Mockito.when(podDirectory.getPodInfo("pod-id")).thenReturn(podInfo);
        given()
                .standaloneSetup(new DirectoryWebhookController(podDirectory, webhookConfiguration))
                .contentType("application/json")
                .body(  "{ " +
                        "    \"appId\" : \"appId-id\"," +
                        "    \"eventType\" : \"APP_ENABLED\"," +
                        "    \"payload\" : {" +
                        "        \"podUrl\" : \"pod-url\"," +
                        "        \"agentUrl\" : \"agent-url\"," +
                        "        \"sessionAuthUrl\" : \"sessionauth-url\"" +
                        "     }" +
                        "}")
                .header("api-key", "super-secret-api-key-1234")
        .when()
                .post("/podInfo")
        .then()
                .assertThat(status().isBadRequest());

        Mockito.verify(podDirectory, never()).getPodInfo(anyString());
    }
}