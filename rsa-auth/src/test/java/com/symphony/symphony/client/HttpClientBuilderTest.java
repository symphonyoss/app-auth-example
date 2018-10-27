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

import feign.okhttp.OkHttpClient;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for {@link HttpClientBuilder}.  Very difficult to test.  We'll just ensure it doesn't blow up.
 *
 * @author Dan Nathanson
 */
public class HttpClientBuilderTest {

    private HttpClientBuilder clientBuilder;
    private SymphonyClientConfiguration config;

    @Before
    public void setup() {
        config = new SymphonyClientConfiguration();
        config.setTruststoreFilename("src/test/resources/cacerts");
    }

    @Test
    public void buildClient() {
        clientBuilder = new HttpClientBuilder(config);

        OkHttpClient client = (OkHttpClient) clientBuilder.buildClient();

        // Not much we can assert besides not null
        assertNotNull(client);
    }

    @Test
    public void buildClientMissingTruststorefile() {
        config.setTruststoreFilename("missing-file");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Couldn't open truststore file");
    }

    @Test
    public void buildClientBadTruststorefile() {
        config.setTruststoreFilename("src/test/resources/bad.truststore");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Couldn't load truststore");
    }


}