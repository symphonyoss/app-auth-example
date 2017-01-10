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
 * Created by Dan Nathanson on 1/6/17.
 */
public class HttpClientBuilderTest {

    private HttpClientBuilder clientBuilder;
    private SymphonyClientConfiguration config;

    @Before
    public void setup() throws IOException {
        config = new SymphonyClientConfiguration();
        config.setKeystoreFilename("src/test/resources/bot.user1.p12");
        config.setKeystorePassword("changeit");
        config.setTruststoreFilename("src/test/resources/cacerts");
    }

    @Test
    public void buildClient() throws Exception {
        clientBuilder = new HttpClientBuilder(config);

        OkHttpClient client = (OkHttpClient) clientBuilder.buildClient();

        // Not much we can assert besides not null
        assertNotNull(client);
    }

    @Test
    public void buildClientMissingKeystoreFile() throws Exception {
        config.setKeystoreFilename("missing-file");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Couldn't open keystore file");
    }

    @Test
    public void buildClientBadKeystorePassword() throws Exception {
        config.setKeystorePassword("bad-password");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Is the password correct");
    }

    @Test
    public void buildClientMissingTruststorefile() throws Exception {
        config.setTruststoreFilename("missing-file");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Couldn't open truststore file");
    }

    @Test
    public void buildClientBadTruststorefile() throws Exception {
        config.setTruststoreFilename("src/test/resources/bad.truststore");

        clientBuilder = new HttpClientBuilder(config);

        assertThatThrownBy(() -> clientBuilder.buildClient())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Couldn't load truststore");
    }


}