package com.symphony.symphony.client;

import com.symphony.example.pods.PodDirectory;
import com.symphony.example.pods.PodInfo;
import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates and manages Feign clients for Symphony pods.  Each pod requires a different Feign client instance since
 * the pod host is configured into the client when it is built.  This factory uses the same client certificates for
 * each AuthenticationClient.  This means that the same certificate must be imported into each pod.  This makes sense
 * since the certificate represents this application itself.
 *
 * Created by Dan Nathanson on 12/21/16.
 */
public class SymphonyClientFactory {
    private Map<String, AuthenticationClient> clients = new HashMap<>();
    private Client okHttpClient;
    private PodDirectory podDirectory;

    @Autowired
    public SymphonyClientFactory(HttpClientBuilder clientBuilder, PodDirectory podDirectory) {
        this.podDirectory = podDirectory;
        okHttpClient = clientBuilder.buildClient();
    }

    /**
     * Returns a Feign client for the Symphony authentication REST endpoints.  Reuses existing client if already
     * built, otherwise builds and caches a new one.
     * @param podId ID of pod for which client is needed
     * @return REST client for pod
     * @throws IllegalStateException if no pod info is present for pod ID
     */
    public AuthenticationClient getAuthenticationClient(String podId) {
        // Gets pod info from PodDirectory. This will throw IllegalStateException if no pod info is available for pod ID
        PodInfo podInfo = podDirectory.getPodInfo(podId);
        final String podHost = podInfo.getPodHost();

        // Return existing (or create, cache and return new instance)
        return clients.computeIfAbsent(podId, k -> Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .client(okHttpClient)
                .target(AuthenticationClient.class, podHost));
    }


}
