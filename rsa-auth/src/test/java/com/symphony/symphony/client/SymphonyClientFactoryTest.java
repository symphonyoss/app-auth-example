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

package com.symphony.symphony.client;

import com.symphony.example.pods.PodDirectory;
import com.symphony.example.pods.PodInfo;
import feign.Client;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link SymphonyClientFactory}
 *
 * @author Dan Nathanson
 */
public class SymphonyClientFactoryTest {

    private SymphonyClientFactory symphonyClientFactory;

    private PodDirectory podDirectory;


    @Before
    public void setup() {
        podDirectory = mock(PodDirectory.class);
        Client httpClient = mock(Client.class);
        HttpClientBuilder httpClientBuilder = mock(HttpClientBuilder.class);
        when(httpClientBuilder.buildClient()).thenReturn(httpClient);
        symphonyClientFactory = new SymphonyClientFactory(httpClientBuilder, podDirectory);
    }

    /**
     * Test getting an AuthenticationClient for a pod.  Subsequent calls with same pod ID should return same object.
     */
    @Test
    public void testGetAuthenticationClient() {

        PodInfo podInfo = new PodInfo();
        podInfo.setCompanyId("company-id");
        PodInfo.PodInfoPayload payload = new PodInfo.PodInfoPayload();
        podInfo.setPayload(payload);
        payload.setPodUrl("pod-url/pod");
        payload.setSessionAuthUrl("sessionauth-url");
        when(podDirectory.getPodInfo("company-id")).thenReturn(podInfo);

        PodInfo anotherPodInfo = new PodInfo();
        PodInfo.PodInfoPayload anotherPayload = new PodInfo.PodInfoPayload();
        anotherPodInfo.setPayload(anotherPayload);
        anotherPodInfo.setCompanyId("another-company-id");
        anotherPayload.setPodUrl("another-pod-url/pod");
        anotherPayload.setSessionAuthUrl("another-sessionauth-url");
        when(podDirectory.getPodInfo("another-company-id")).thenReturn(anotherPodInfo);


        AuthenticationClient authenticationClient = symphonyClientFactory.getAuthenticationClient("company-id");
        assertNotNull("Should return something", authenticationClient);

        AuthenticationClient anotherAuthenticationClient = symphonyClientFactory.getAuthenticationClient("another-company-id");
        assertNotNull("Should return something", anotherAuthenticationClient);

        assertNotSame("Should be different objects", authenticationClient, anotherAuthenticationClient);

        AuthenticationClient sameAuthenticationClient = symphonyClientFactory.getAuthenticationClient("company-id");

        assertSame("Should return same object", authenticationClient, sameAuthenticationClient);




    }


}